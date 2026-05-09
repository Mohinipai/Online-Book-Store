package com.bookstore.service.impl;

import com.bookstore.dto.OrderDto;
import com.bookstore.dto.OrderItemDto;
import com.bookstore.exception.ApiException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.*;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.CartService;
import com.bookstore.service.EmailService;
import com.bookstore.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private UserRepository userRepository;
    private CartService cartService;
    private BookRepository bookRepository;
    private EmailService emailService;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
    }

    @Override
    @Transactional
    public OrderDto placeOrder() {
        User user = getAuthenticatedUser();
        Cart cart = user.getCart();

        if (cart.getCartItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot place order, cart is empty.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.getTotalPrice());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
            // Check stock again
            Book book = cartItem.getBook();
            if (book.getStockQuantity() < cartItem.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock for book: " + book.getTitle());
            }

            // Deduct stock
            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            bookRepository.save(book);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setBook(book);
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        // Clear cart after placing order
        cartService.clearCart(cart.getId());

        // Send Email asynchronously (or synchronously for now)
        emailService.sendOrderConfirmationEmail(
                user.getEmail(),
                user.getName(),
                savedOrder.getId(),
                savedOrder.getTotalAmount().toString()
        );

        return mapToDto(savedOrder);
    }

    @Override
    public List<OrderDto> getUserOrders() {
        User user = getAuthenticatedUser();
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new ResourceNotFoundException("Order", "id", orderId)
        );
        return mapToDto(order);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getName());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDto> itemDtos = order.getOrderItems().stream().map(item -> {
            OrderItemDto itemDto = new OrderItemDto();
            itemDto.setId(item.getId());
            itemDto.setBookId(item.getBook().getId());
            itemDto.setBookTitle(item.getBook().getTitle());
            itemDto.setPrice(item.getPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubTotal(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).collect(Collectors.toList());

        dto.setOrderItems(itemDtos);
        return dto;
    }public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository, CartService cartService, BookRepository bookRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.bookRepository = bookRepository;
        this.emailService = emailService;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CartService getCartService() {
        return cartService;
    }

    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

}
