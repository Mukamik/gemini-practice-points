package com.example.demo.graphql;

import com.example.demo.model.Author;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @QueryMapping
    public List<Book> books(@Argument String isbn, @Argument String title) {
        return bookService.getBooks(isbn, title);
    }

    @QueryMapping
    public Book book(@Argument String isbn) {
        return bookService.getBookByIsbn(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with isbn: " + isbn));
    }

    @MutationMapping
    public Book addBook(@Argument BookInput book) {
        return bookService.addBook(book);
    }

    @MutationMapping
    public Book updateBook(@Argument String isbn, @Argument UpdateBookInput book) {
        return bookService.updateBook(isbn, book);
    }

    @MutationMapping
    public boolean deleteBook(@Argument String isbn) {
        return bookService.deleteBook(isbn);
    }

    @MutationMapping
    public Book linkAuthorToBook(@Argument Integer authorId, @Argument String isbn) {
        return bookService.linkAuthorToBook(authorId, isbn);
    }

    @MutationMapping
    public Book unlinkAuthorFromBook(@Argument Integer authorId, @Argument String isbn) {
        return bookService.unlinkAuthorFromBook(authorId, isbn);
    }

    @SchemaMapping(typeName = "Book", field = "authors")
    public Set<Author> getAuthors(Book book) {
        return book.getAuthors(); // Assumes lazy loading works or authors are fetched
    }

    @SchemaMapping(typeName = "Book", field = "category")
    public Category getCategory(Book book) {
        return book.getCategory(); // Assumes lazy loading works or category is fetched
    }
}