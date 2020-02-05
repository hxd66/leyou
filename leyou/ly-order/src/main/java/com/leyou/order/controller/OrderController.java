package com.leyou.order.controller;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.service.OrderService;
import com.leyou.order.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("order")
    public ResponseEntity<Long> createOrder(@RequestBody @Valid OrderDTO orderDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(orderDTO));
    }

    @GetMapping("{id}")
    public ResponseEntity<OrderVO> queryOrderById(@PathVariable("id") Long orderId){
        return ResponseEntity.ok(orderService.queryOrderById(orderId));
    }

    /**
     * 下单生成支付链接
     * @param orderId
     * @return
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> getPayUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.createPayUrl(orderId));
    }

    /**
     * 查询支付状态
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryPayState(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryPayStatus(orderId));
    }
}
