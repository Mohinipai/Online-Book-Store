package com.bookstore.controller;

import com.bookstore.dto.CartDto;
import com.bookstore.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addProductToCart(
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") Integer quantity) {
        CartDto cartDto = cartService.addProductToCart(bookId, quantity);
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartDto> updateCartItemQuantity(
            @PathVariable("cartItemId") Long cartItemId,
            @RequestParam("quantity") Integer quantity) {
        CartDto cartDto = cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(cartDto);
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartDto> removeProductFromCart(
            @PathVariable("cartItemId") Long cartItemId) {
        CartDto cartDto = cartService.removeProductFromCart(cartItemId);
        return ResponseEntity.ok(cartDto);
    }public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    public CartService getCartService() {
        return cartService;
    }

    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

}
