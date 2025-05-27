package com.example.demo.service;

import com.example.demo.graphql.BookInput;
import com.example.demo.graphql.UpdateBookInput;
import com.example.demo.model.Author;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> getBooks(String isbn, String title) {
        if (isbn != null && !isbn.trim().isEmpty() && title != null && !title.trim().isEmpty()) {
            return bookRepository.findByIsbnContainingIgnoreCaseAndTitleContainingIgnoreCase(isbn, title);
        } else if (isbn != null && !isbn.trim().isEmpty()) {
            return bookRepository.findByIsbnContainingIgnoreCase(isbn);
        } else if (title != null && !title.trim().isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCase(title);
        }
        return bookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Book> getBookByIsbn(String isbn) {
        return bookRepository.findById(isbn);
    }

    @Transactional
    public Book addBook(BookInput input) {
        Book book = new Book();
        book.setIsbn(input.isbn());
        book.setTitle(input.title());
        book.setPages(input.pages());
        book.setYear(input.year()); // Corrected from SP bug (Year = Pages)

        if (input.categoryId() != null) {
            Category category = categoryRepository.findById(input.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + input.categoryId()));
            book.setCategory(category);
        }

        if (input.authorIds() != null && !input.authorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>(authorRepository.findAllById(input.authorIds()));
            if (authors.size() != input.authorIds().size()) {
                throw new EntityNotFoundException("One or more authors not found.");
            }
            book.setAuthors(authors);
        }
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(String isbn, UpdateBookInput input) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with isbn: " + isbn));

        if (input.title() != null) book.setTitle(input.title());
        if (input.pages() != null) book.setPages(input.pages());
        if (input.year() != null) book.setYear(input.year()); // Corrected

        if (input.categoryId() != null) {
            Category category = categoryRepository.findById(input.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + input.categoryId()));
            book.setCategory(category);
        } else if (input.categoryId() == null && input.title() != null) { // Check if explicitly setting category to null
             // If UpdateBookInput had a way to signal "set category to null", handle here.
             // For now, if categoryId is not provided, it's not changed. To remove, one might pass a special value or have a separate mutation.
        }


        if (input.authorIds() != null) { // If authorIds is provided (even if empty), update the set
            Set<Author> authors = new HashSet<>(authorRepository.findAllById(input.authorIds()));
             if (authors.size() != input.authorIds().size() && !input.authorIds().isEmpty()) { // if input authorIds is not empty but not all found
                throw new EntityNotFoundException("One or more authors not found for updating links.");
            }
            book.setAuthors(authors);
        }
        return bookRepository.save(book);
    }

    @Transactional
    public boolean deleteBook(String isbn) {
        Book book = bookRepository.findById(isbn).orElse(null);
        if (book == null) {
            return false; // Or throw EntityNotFoundException
        }
        // Book entity owns the @ManyToMany relationship with Author.
        // Deleting the book should make JPA remove entries from the AuthorBook join table.
        // The DB constraint FK_AuthorBook_Books is ON DELETE NO ACTION, so JPA must handle this.
        bookRepository.delete(book);
        return true;
    }

    @Transactional
    public Book linkAuthorToBook(Integer authorId, String isbn) {
        Book book = getBookByIsbn(isbn).orElseThrow(() -> new EntityNotFoundException("Book not found: " + isbn));
        Author author = authorRepository.findById(authorId).orElseThrow(() -> new EntityNotFoundException("Author not found: " + authorId));
        book.getAuthors().add(author);
        // author.getBooks().add(book); // Not needed if Book is the owner of the relationship
        return bookRepository.save(book);
    }

    @Transactional
    public Book unlinkAuthorFromBook(Integer authorId, String isbn) {
        Book book = getBookByIsbn(isbn).orElseThrow(() -> new EntityNotFoundException("Book not found: " + isbn));
        Author author = authorRepository.findById(authorId).orElseThrow(() -> new EntityNotFoundException("Author not found: " + authorId));
        book.getAuthors().remove(author);
        // author.getBooks().remove(book); // Not needed if Book is the owner
        return bookRepository.save(book);
    }
}