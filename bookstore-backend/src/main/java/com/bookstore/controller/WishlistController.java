package com.bookstore.controller;

import com.bookstore.dto.BookDto;
import com.bookstore.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private WishlistService wishlistService;

    @PostMapping("/{bookId}")
    public ResponseEntity<String> addToWishlist(@PathVariable("bookId") Long bookId) {
        wishlistService.addToWishlist(bookId);
        return ResponseEntity.ok("Added to wishlist successfully!");
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable("bookId") Long bookId) {
        wishlistService.removeFromWishlist(bookId);
        return ResponseEntity.ok("Removed from wishlist successfully!");
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getUserWishlist() {
        return ResponseEntity.ok(wishlistService.getUserWishlist());
    }public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    public WishlistService getWishlistService() {
        return wishlistService;
    }

    public void setWishlistService(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

}
