package com.bookstore.dto;


import java.math.BigDecimal;
import java.util.List;

public class CartDto {
    private Long id;
    private List<CartItemDto> cartItems;
    private BigDecimal totalPrice;

    public CartDto() {}

    public CartDto(Long id, List<CartItemDto> cartItems, BigDecimal totalPrice) {
        this.id = id;
        this.cartItems = cartItems;
        this.totalPrice = totalPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItemDto> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDto> cartItems) {
        this.cartItems = cartItems;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

}
