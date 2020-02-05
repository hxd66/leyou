package com.leyou.order.service;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.vo.OrderVO;

public interface OrderService {
    Long createOrder(OrderDTO orderDTO);

    OrderVO queryOrderById(Long orderId);

    String createPayUrl(Long orderId);

    Integer queryPayStatus(Long id);
}
