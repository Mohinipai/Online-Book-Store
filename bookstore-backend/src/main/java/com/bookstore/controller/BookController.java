package com.bookstore.controller;

import com.bookstore.dto.BookDto;
import com.bookstore.dto.PageResponseDto;
import com.bookstore.service.BookService;
import com.bookstore.service.FileUploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService bookService;
    private FileUploadService fileUploadService;

    // Admin can add books
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<BookDto> addBook(@Valid @RequestBody BookDto bookDto) {
        BookDto savedBook = bookService.addBook(bookDto);
        return new ResponseEntity<>(savedBook, HttpStatus.CREATED);
    }

    // Public get all books (with pagination and sorting)
    @GetMapping
    public ResponseEntity<PageResponseDto<BookDto>> getAllBooks(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(bookService.searchBooks(search, pageNo, pageSize, sortBy, sortDir));
        }
        return ResponseEntity.ok(bookService.getAllBooks(pageNo, pageSize, sortBy, sortDir));
    }

    // Public get book by id
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable("id") Long id) {
        BookDto bookDto = bookService.getBookById(id);
        return ResponseEntity.ok(bookDto);
    }

    // Admin can update book
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(@Valid @RequestBody BookDto bookDto, 
                                              @PathVariable("id") Long id) {
        BookDto updatedBook = bookService.updateBook(bookDto, id);
        return ResponseEntity.ok(updatedBook);
    }

    // Admin can delete book
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok("Book deleted successfully!");
    }

    // Admin can upload book image
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/image")
    public ResponseEntity<BookDto> uploadBookImage(@PathVariable("id") Long id,
                                                   @RequestParam("file") MultipartFile file) {
        BookDto bookDto = bookService.getBookById(id);
        
        try {
            String imageUrl = fileUploadService.uploadFile(file);
            bookDto.setImageUrl(imageUrl);
            BookDto updatedBook = bookService.updateBook(bookDto, id);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }public BookController(BookService bookService, FileUploadService fileUploadService) {
        this.bookService = bookService;
        this.fileUploadService = fileUploadService;
    }

    public BookService getBookService() {
        return bookService;
    }

    public void setBookService(BookService bookService) {
        this.bookService = bookService;
    }

    public FileUploadService getFileUploadService() {
        return fileUploadService;
    }

    public void setFileUploadService(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

}
