package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    /**
     * http://api.leyou.com/api/cart
     * @param cart 添加到购物车
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询购物车
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList(){
        List<Cart> carts = cartService.queryCartList();
        if (CollectionUtils.isEmpty(carts)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * http://api.leyou.com/api/cart
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam("id") Long skuId,
                                          @RequestParam("num") Integer num){
        cartService.updateNum(skuId,num);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId")String skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
    /**
     * 将登陆前和登录后的购物车合并
     */
    @PostMapping("list")
    public ResponseEntity<Void> addCartList(@RequestBody List<Cart> cartList){
        cartService.addCartList(cartList);
        return ResponseEntity.ok().build();
    }
}
