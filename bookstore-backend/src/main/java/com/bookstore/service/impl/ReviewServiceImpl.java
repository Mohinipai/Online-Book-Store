package com.bookstore.service.impl;

import com.bookstore.dto.ReviewDto;
import com.bookstore.exception.ApiException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.Review;
import com.bookstore.model.User;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.ReviewRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    private ReviewRepository reviewRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
    }

    @Override
    @Transactional
    public ReviewDto addReview(Long bookId, ReviewDto reviewDto) {
        User user = getAuthenticatedUser();
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new ResourceNotFoundException("Book", "id", bookId)
        );

        if (reviewRepository.existsByUserIdAndBookId(user.getId(), book.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You have already reviewed this book!");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        Review savedReview = reviewRepository.save(review);

        // Update Book Rating
        List<Review> allReviews = reviewRepository.findByBookId(bookId);
        double totalRating = allReviews.stream().mapToInt(Review::getRating).sum();
        book.setAverageRating(totalRating / allReviews.size());
        book.setReviewCount(allReviews.size());
        bookRepository.save(book);

        return mapToDto(savedReview);
    }

    @Override
    public List<ReviewDto> getReviewsByBookId(Long bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ReviewDto mapToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getName());
        dto.setBookId(review.getBook().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }public ReviewServiceImpl(ReviewRepository reviewRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public ReviewRepository getReviewRepository() {
        return reviewRepository;
    }

    public void setReviewRepository(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
