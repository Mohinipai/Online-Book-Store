package com.bookstore.service;

import com.bookstore.dto.BookDto;
import java.util.List;

public interface WishlistService {
    void addToWishlist(Long bookId);
    void removeFromWishlist(Long bookId);
    List<BookDto> getUserWishlist();
}
