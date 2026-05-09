package com.bookstore.config;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    @Autowired
    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (bookRepository.count() == 0) {
            seedBooks();
        }
    }

    private void seedBooks() {
        List<String> categories = Arrays.asList(
                "Fiction", "Sci-Fi", "Mystery", "Romance", "History", "Memoir", 
                "Self Help", "Tech", "Fantasy", "Thriller", "Business", "Biography"
        );

        List<String> authors = Arrays.asList(
                "Matt Haig", "James Clear", "Andy Weir", "Kazuo Ishiguro", "Frank Herbert",
                "Tara Westover", "Alex Michaelides", "Delia Owens", "Yuval Noah Harari", 
                "Madeline Miller", "David Thomas", "J.R.R. Tolkien", "George R.R. Martin"
        );

        Random random = new Random();

        for (String category : categories) {
            for (int i = 1; i <= 10; i++) {
                Book book = new Book();
                book.setTitle(category + " Masterpiece Vol " + i);
                book.setAuthor(authors.get(random.nextInt(authors.size())));
                
                // Random price between 9.99 and 29.99
                double randomPrice = 9.99 + (20.0 * random.nextDouble());
                book.setPrice(BigDecimal.valueOf(Math.round(randomPrice * 100.0) / 100.0));
                
                book.setCategory(category);
                book.setDescription("A highly acclaimed " + category.toLowerCase() + " book that takes you on an unforgettable journey. Perfect for readers who enjoy exploring " + category.toLowerCase() + " topics.");
                book.setStockQuantity(50 + random.nextInt(100)); // 50 to 149
                
                // Random rating between 3.5 and 5.0
                double rating = 3.5 + (1.5 * random.nextDouble());
                book.setAverageRating(Math.round(rating * 10.0) / 10.0);
                
                book.setReviewCount(random.nextInt(500));
                book.setImageUrl("https://picsum.photos/seed/" + category.replaceAll(" ", "") + i + "/400/600");

                bookRepository.save(book);
            }
        }
        System.out.println("Data Seeder: Successfully inserted 120 books into the database!");
    }
}
