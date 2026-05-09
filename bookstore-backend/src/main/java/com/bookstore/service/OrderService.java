package com.bookstore.service;

import com.bookstore.dto.OrderDto;
import java.util.List;

public interface OrderService {
    OrderDto placeOrder();
    List<OrderDto> getUserOrders();
    OrderDto getOrderById(Long orderId);
    List<OrderDto> getAllOrders(); // For Admin
}
