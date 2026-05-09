package com.bookstore.service;

import com.bookstore.dto.ReviewDto;
import java.util.List;

public interface ReviewService {
    ReviewDto addReview(Long bookId, ReviewDto reviewDto);
    List<ReviewDto> getReviewsByBookId(Long bookId);
}
