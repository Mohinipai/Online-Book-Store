package com.bookstore.controller;

import com.bookstore.dto.OrderDto;
import com.bookstore.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> placeOrder() {
        OrderDto orderDto = orderService.placeOrder();
        return new ResponseEntity<>(orderDto, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        List<OrderDto> orders = orderService.getUserOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable("id") Long id) {
        OrderDto orderDto = orderService.getOrderById(id);
        return ResponseEntity.ok(orderDto);
    }

    // Admin can view all orders
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

}
