package com.bookstore.service;

import com.bookstore.dto.BookDto;
import com.bookstore.dto.PageResponseDto;

public interface BookService {
    BookDto addBook(BookDto bookDto);
    BookDto updateBook(BookDto bookDto, Long id);
    void deleteBook(Long id);
    BookDto getBookById(Long id);
    PageResponseDto<BookDto> getAllBooks(int pageNo, int pageSize, String sortBy, String sortDir);
    PageResponseDto<BookDto> searchBooks(String keyword, int pageNo, int pageSize, String sortBy, String sortDir);
}
