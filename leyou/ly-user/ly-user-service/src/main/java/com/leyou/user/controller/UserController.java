package com.leyou.user.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RefreshScope
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 判断手机或者用户名是否存在
     * @param param  手机或用户名
     * @param type 类型 1：用户名    2：手机
     * @return
     */
    @GetMapping("check/{param}/{type}")
    @ApiOperation(value = "校验结果有效，true或false代表可用或不可用")
    @ApiResponses({
            @ApiResponse(code = 200,message = "校验结果有效，true或false代表可用或不可用"),
            @ApiResponse(code = 400,message = "请求参数有误，比如type不是指定值")
    })
    public ResponseEntity<Boolean> checkData(
            @ApiParam(value = "要校验的数据",example = "lisi") @PathVariable("param")String param,
            @ApiParam(value = "数据类型,1：用户名 2：手机号",example = "1")@PathVariable("type")Integer type){
        return ResponseEntity.ok(userService.checkData(param,type));
    }
    /**
     * 发送短信验证码
     * @param phone
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone")String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 完成用户注册
     * @param user user对象
     * @param code 验证码，不往数据库存
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code")String code){
        //通过result.hashErrors来判断是否有错误，然后通过result.getFieldErrors来获取错误信息
        if (result.hasErrors()){
            String msg = result.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("|"));
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR, msg);

        }
        userService.register(user, code);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据用户名和密码查询用户
     */
    @GetMapping("query")
    public ResponseEntity<UserDTO> queryUserByUsernameAndPassword(
            @RequestParam("username")String username,@RequestParam("password")String password
    ){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }


}
