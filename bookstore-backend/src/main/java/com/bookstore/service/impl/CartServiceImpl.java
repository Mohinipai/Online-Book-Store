package com.bookstore.service.impl;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.CartItemDto;
import com.bookstore.exception.ApiException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.model.Book;
import com.bookstore.model.Cart;
import com.bookstore.model.CartItem;
import com.bookstore.model.User;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User", "email", email)
        );
    }

    private Cart getUserCart() {
        User user = getAuthenticatedUser();
        return cartRepository.findByUserId(user.getId()).orElseThrow(
                () -> new ResourceNotFoundException("Cart", "userId", user.getId())
        );
    }

    @Override
    @Transactional
    public CartDto addProductToCart(Long bookId, Integer quantity) {
        Cart cart = getUserCart();
        Book book = bookRepository.findById(bookId).orElseThrow(
                () -> new ResourceNotFoundException("Book", "id", bookId)
        );

        if (book.getStockQuantity() < quantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock available for book: " + book.getTitle());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getBook().getId().equals(bookId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            if(item.getQuantity() > book.getStockQuantity()){
                 throw new ApiException(HttpStatus.BAD_REQUEST, "Total quantity exceeds stock for book: " + book.getTitle());
            }
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setBook(book);
            newItem.setQuantity(quantity);
            newItem.setPrice(book.getPrice());
            cart.getCartItems().add(newItem);
        }

        updateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    @Transactional
    public CartDto updateCartItemQuantity(Long cartItemId, Integer quantity) {
        Cart cart = getUserCart();
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new ResourceNotFoundException("CartItem", "id", cartItemId)
        );

        // Ensure cart item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Cart item does not belong to your cart");
        }

        if (cartItem.getBook().getStockQuantity() < quantity) {
             throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock available");
        }

        cartItem.setQuantity(quantity);
        updateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    @Transactional
    public CartDto removeProductFromCart(Long cartItemId) {
        Cart cart = getUserCart();
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(
                () -> new ResourceNotFoundException("CartItem", "id", cartItemId)
        );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Cart item does not belong to your cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        
        updateCartTotal(cart);
        Cart savedCart = cartRepository.save(cart);
        return mapToDto(savedCart);
    }

    @Override
    public CartDto getCart() {
        Cart cart = getUserCart();
        return mapToDto(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(
            () -> new ResourceNotFoundException("Cart", "id", cartId)
        );
        cart.getCartItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    private void updateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            BigDecimal itemTotal = item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setPrice(item.getBook().getPrice()); // Update current price
            total = total.add(itemTotal);
        }
        cart.setTotalPrice(total);
    }

    private CartDto mapToDto(Cart cart) {
        CartDto cartDto = new CartDto();
        cartDto.setId(cart.getId());
        cartDto.setTotalPrice(cart.getTotalPrice());

        List<CartItemDto> cartItemDtos = cart.getCartItems().stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setId(item.getId());
            itemDto.setBookId(item.getBook().getId());
            itemDto.setBookTitle(item.getBook().getTitle());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setPrice(item.getPrice());
            itemDto.setSubTotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return itemDto;
        }).collect(Collectors.toList());

        cartDto.setCartItems(cartItemDtos);
        return cartDto;
    }public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public CartRepository getCartRepository() {
        return cartRepository;
    }

    public void setCartRepository(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartItemRepository getCartItemRepository() {
        return cartItemRepository;
    }

    public void setCartItemRepository(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
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
