package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;

import java.util.List;

public interface CartService {
    void addCart(Cart cart);

    List<Cart> queryCartList();

    void updateNum(Long skuId, Integer num);

    void deleteCart(String skuId);

    void addCartList(List<Cart> cartList);
}
