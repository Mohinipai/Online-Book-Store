package com.bookstore.service.impl;

import com.bookstore.dto.BookDto;
import com.bookstore.dto.PageResponseDto;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private BookRepository bookRepository;

    @Override
    public BookDto addBook(BookDto bookDto) {
        Book book = mapToEntity(bookDto);
        Book savedBook = bookRepository.save(book);
        return mapToDto(savedBook);
    }

    @Override
    public BookDto updateBook(BookDto bookDto, Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Book", "id", id)
        );

        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setDescription(bookDto.getDescription());
        book.setCategory(bookDto.getCategory());
        book.setPrice(bookDto.getPrice());
        book.setStockQuantity(bookDto.getStockQuantity());
        book.setImageUrl(bookDto.getImageUrl());

        Book updatedBook = bookRepository.save(book);
        return mapToDto(updatedBook);
    }

    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Book", "id", id)
        );
        bookRepository.delete(book);
    }

    @Override
    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Book", "id", id)
        );
        return mapToDto(book);
    }

    @Override
    public PageResponseDto<BookDto> getAllBooks(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Book> books = bookRepository.findAll(pageable);
        List<Book> listOfBooks = books.getContent();

        List<BookDto> content = listOfBooks.stream().map(this::mapToDto).collect(Collectors.toList());

        PageResponseDto<BookDto> bookResponse = new PageResponseDto<>();
        bookResponse.setContent(content);
        bookResponse.setPageNo(books.getNumber());
        bookResponse.setPageSize(books.getSize());
        bookResponse.setTotalElements(books.getTotalElements());
        bookResponse.setTotalPages(books.getTotalPages());
        bookResponse.setLast(books.isLast());

        return bookResponse;
    }

    @Override
    public PageResponseDto<BookDto> searchBooks(String keyword, int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Book> books = bookRepository.searchBooks(keyword, pageable);
        List<Book> listOfBooks = books.getContent();

        List<BookDto> content = listOfBooks.stream().map(this::mapToDto).collect(Collectors.toList());

        PageResponseDto<BookDto> bookResponse = new PageResponseDto<>();
        bookResponse.setContent(content);
        bookResponse.setPageNo(books.getNumber());
        bookResponse.setPageSize(books.getSize());
        bookResponse.setTotalElements(books.getTotalElements());
        bookResponse.setTotalPages(books.getTotalPages());
        bookResponse.setLast(books.isLast());

        return bookResponse;
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
    }

    private Book mapToEntity(BookDto bookDto){
        Book book = new Book();
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setDescription(bookDto.getDescription());
        book.setCategory(bookDto.getCategory());
        book.setPrice(bookDto.getPrice());
        book.setStockQuantity(bookDto.getStockQuantity());
        book.setImageUrl(bookDto.getImageUrl());
        return book;
    }public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public void setBookRepository(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

}
