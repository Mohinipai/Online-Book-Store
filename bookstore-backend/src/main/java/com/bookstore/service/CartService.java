package com.bookstore.service;

import com.bookstore.dto.CartDto;

public interface CartService {
    CartDto addProductToCart(Long bookId, Integer quantity);
    CartDto updateCartItemQuantity(Long cartItemId, Integer quantity);
    CartDto removeProductFromCart(Long cartItemId);
    CartDto getCart();
    void clearCart(Long cartId);
}
