package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "ly:cart:uid";

    @Override
    public void addCart(Cart cart) {
        //获取当前用户
        String key = KEY_PREFIX + UserHolder.getUserId();
        //根据用户id获取redis中的hash对象
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);

        //获取商品的sku的id
        String skuId = cart.getSkuId().toString();
        //获取本次sku的对应数量
        Integer skuNum = cart.getNum();
        //判断要添加的商品是否存在
        Boolean hasKey = hashOps.hasKey(skuId);
        if (hasKey != null && hasKey){
            //存在，修改数量
            cart = JsonUtils.toBean(hashOps.get(skuId),Cart.class);
            cart.setNum(skuNum + cart.getNum());
        }
        //写入redis
        hashOps.put(skuId,JsonUtils.toString(cart));
    }

    /**
     * 查询购物车
     * @return
     */
    @Override
    public List<Cart> queryCartList() {
        //获取登录用户
        String key = KEY_PREFIX + UserHolder.getUserId();
        //判断是否存在购物车，没有就抛异常
        Boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey == null || !hasKey){
            //不存在，直接返回
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        //判断购物车是否有数据，没有就抛异常
        Long size = hashOps.size();
        if (size == null || size < 1){
            //不存在，直接返回
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        List<String> carts = hashOps.values();
        //查询购物车数据
        return carts.stream().map(json -> JsonUtils.toBean(json,Cart.class))
                .collect(Collectors.toList());
    }

    /**
     * 修改购物车
     * @param skuId
     * @param num
     */
    @Override
    public void updateNum(Long skuId, Integer num) {
        //获取当前用户
        String key = KEY_PREFIX + UserHolder.getUserId();
        //获取hash操作的对象
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        String hashKey = skuId.toString();
        Boolean boo = hashOps.hasKey(hashKey);
        if (boo == null || !boo){
            log.error("购物车商品不存在，用户：{}, 商品：{}", key, skuId);
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        //查找购物车商品
        Cart cart = JsonUtils.toBean(hashOps.get(hashKey), Cart.class);
        //直接修改数量即可
        cart.setNum(num);
        //写回redis
        hashOps.put(hashKey,JsonUtils.toString(cart));
    }

    /**
     * 删除购物车商品
     * @param skuId
     */
    @Override
    public void deleteCart(String skuId) {
        //获取当前用户
        String key = KEY_PREFIX + UserHolder.getUserId();
        redisTemplate.boundHashOps(key).delete(skuId);
    }

    /**
     * 合并购物车
     * @param cartList
     */
    @Override
    public void addCartList(List<Cart> cartList) {
        for (Cart cart : cartList) {
            addCart(cart);
        }
    }
}
