package com.bookstore.controller;

import com.bookstore.dto.ReviewDto;
import com.bookstore.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class ReviewController {

    private ReviewService reviewService;

    @PostMapping("/{bookId}/reviews")
    public ResponseEntity<ReviewDto> addReview(@PathVariable("bookId") Long bookId,
                                               @Valid @RequestBody ReviewDto reviewDto) {
        ReviewDto savedReview = reviewService.addReview(bookId, reviewDto);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviews(@PathVariable("bookId") Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId));
    }public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    public ReviewService getReviewService() {
        return reviewService;
    }

    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

}
