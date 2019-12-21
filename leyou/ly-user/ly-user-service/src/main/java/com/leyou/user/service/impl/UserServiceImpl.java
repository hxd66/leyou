package com.leyou.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.RegexUtils;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 判断手机或者用户名是否存在
     * @param param  手机或用户名
     * @param type 类型 1：用户名    2：手机
     * @return
     */
    @Override
    public Boolean checkData(String param, Integer type) {
        User user = new User();
        switch (type){
            case 1:
                user.setUsername(param);
                break;
            case 2:
                user.setPhone(param);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>(user);
        //如果count是0，代表数据库里没有，可以注册
        Integer count = userMapper.selectCount(wrapper);
        return count == 0;
    }

    /**
     * 发送短信验证码
     * @param phone
     */
    @Override
    public void sendCode(String phone) {
        //验证手机号格式
        if (!RegexUtils.isPhone(phone)){
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }
        //生成验证码
        String code = RandomStringUtils.randomNumeric(6);

        //保存到redis中,设置时间,这里测试，就不设置时间了
//        redisTemplate.boundValueOps(phone).set(code,5, TimeUnit.MINUTES);
        redisTemplate.boundValueOps(phone).set(code);

        //发送RabbitMQ消息到ly-sms
        HashMap<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME,VERIFY_CODE_KEY,msg);
    }

    /**
     * 用户注册
     * @param user
     * @param code
     */
    @Override
    public void register(User user, String code) {
        //先校验验证码，取出redis中的验证码
        String cacheCode = redisTemplate.boundValueOps(user.getPhone()).get();
        //比较验证码
        if (!StringUtils.equals(code,cacheCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
        //对密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //写入数据库
        int count = userMapper.insert(user);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 根据用户名和密码查询user
     * @param username
     * @param password
     * @return
     */
    @Override
    public UserDTO queryUserByUsernameAndPassword(String username, String password) {
        //根据用户名查询
        User user = new User();
        user.setUsername(username);
        User userDB = userMapper.selectOne(new QueryWrapper<>(user));
        //判断是否存在
        if (userDB == null){
            // 用户名错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        //校验密码
        if (!passwordEncoder.matches(password,userDB.getPassword())){
            // 密码错误
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        return BeanHelper.copyProperties(userDB,UserDTO.class);
    }
}
