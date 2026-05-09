package com.bookstore.service.impl;

import com.bookstore.dto.BookDto;
import com.bookstore.exception.ApiException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.User;
import com.bookstore.model.Wishlist;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    private WishlistRepository wishlistRepository;
    private UserRepository userRepository;
    private BookRepository bookRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
    }

    @Override
    @Transactional
    public void addToWishlist(Long bookId) {
        User user = getAuthenticatedUser();
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new ResourceNotFoundException("Book", "id", bookId)
        );

        if (wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Book is already in wishlist");
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setBook(book);
        wishlistRepository.save(wishlist);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long bookId) {
        User user = getAuthenticatedUser();
        if (!wishlistRepository.existsByUserIdAndBookId(user.getId(), bookId)) {
            throw new ResourceNotFoundException("Wishlist Item", "bookId", bookId);
        }
        wishlistRepository.deleteByUserIdAndBookId(user.getId(), bookId);
    }

    @Override
    public List<BookDto> getUserWishlist() {
        User user = getAuthenticatedUser();
        List<Wishlist> wishlists = wishlistRepository.findByUserId(user.getId());
        
        return wishlists.stream().map(wishlist -> mapToDto(wishlist.getBook())).collect(Collectors.toList());
    }

    private BookDto mapToDto(Book book){
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setDescription(book.getDescription());
        bookDto.setCategory(book.getCategory());
        bookDto.setPrice(book.getPrice());
        bookDto.setStockQuantity(book.getStockQuantity());
        bookDto.setImageUrl(book.getImageUrl());
        bookDto.setAverageRating(book.getAverageRating());
        bookDto.setReviewCount(book.getReviewCount());
        return bookDto;
    }public WishlistServiceImpl(WishlistRepository wishlistRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public WishlistRepository getWishlistRepository() {
        return wishlistRepository;
    }

    public void setWishlistRepository(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

}
