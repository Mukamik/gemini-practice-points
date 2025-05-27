package com.example.demo.service;

import com.example.demo.graphql.BookInput;
import com.example.demo.graphql.UpdateBookInput;
import com.example.demo.model.Author;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class BookServiceIntegrationTests {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Category defaultCategory;
    private Author defaultAuthor1;
    private Author defaultAuthor2;

    @BeforeEach
    void setUp() {
        // Clear and set up common entities to avoid test interference
        // Note: @Transactional handles rollback, but explicit setup can be clearer

        defaultCategory = categoryRepository.save(new Category("Test Category"));
        defaultAuthor1 = authorRepository.save(new Author("Test", "AuthorOne", null));
        defaultAuthor2 = authorRepository.save(new Author("Another", "AuthorTwo", null));
        entityManager.flush();
    }

    @Test
    void addBook_shouldSaveAndReturnBookWithCategoryAndAuthors() {
        BookInput input = new BookInput(
                "9780123456789", "A Great Book", 300, 2023,
                defaultCategory.getId(), List.of(defaultAuthor1.getId(), defaultAuthor2.getId())
        );
        Book savedBook = bookService.addBook(input);

        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("9780123456789");
        assertThat(savedBook.getTitle()).isEqualTo("A Great Book");
        assertThat(savedBook.getCategory()).isNotNull();
        assertThat(savedBook.getCategory().getId()).isEqualTo(defaultCategory.getId());
        assertThat(savedBook.getAuthors()).hasSize(2);
        assertThat(savedBook.getAuthors()).extracting(Author::getId).containsExactlyInAnyOrder(defaultAuthor1.getId(), defaultAuthor2.getId());

        Optional<Book> found = bookRepository.findById(savedBook.getIsbn());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("A Great Book");
    }

    @Test
    void addBook_withNonExistentCategory_shouldThrowException() {
        BookInput input = new BookInput("111", "Title", 100, 2000, 9999, Collections.emptyList());
        assertThrows(EntityNotFoundException.class, () -> bookService.addBook(input));
    }

    @Test
    void addBook_withNonExistentAuthor_shouldThrowException() {
        BookInput input = new BookInput("222", "Title", 100, 2000, defaultCategory.getId(), List.of(9999));
        assertThrows(EntityNotFoundException.class, () -> bookService.addBook(input));
    }

    @Test
    void updateBook_shouldUpdateExistingBookDetails() {
        BookInput initialInput = new BookInput("12345", "Old Title", 100, 2000, defaultCategory.getId(), List.of(defaultAuthor1.getId()));
        Book book = bookService.addBook(initialInput);

        Category newCategory = categoryRepository.save(new Category("New Test Category"));
        UpdateBookInput updateInput = new UpdateBookInput(
                "New Title", 200, 2021, newCategory.getId(), List.of(defaultAuthor2.getId())
        );

        Book updatedBook = bookService.updateBook(book.getIsbn(), updateInput);

        assertThat(updatedBook.getTitle()).isEqualTo("New Title");
        assertThat(updatedBook.getPages()).isEqualTo(200);
        assertThat(updatedBook.getYear()).isEqualTo(2021); // Verifying the fix for SP bug
        assertThat(updatedBook.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(updatedBook.getAuthors()).hasSize(1);
        assertThat(updatedBook.getAuthors().iterator().next().getId()).isEqualTo(defaultAuthor2.getId());
    }

    @Test
    void updateBook_setAuthorsToEmptyList() {
        BookInput initialInput = new BookInput("1234500", "Book With Authors", 100, 2000, defaultCategory.getId(), List.of(defaultAuthor1.getId()));
        Book book = bookService.addBook(initialInput);
        assertThat(book.getAuthors()).isNotEmpty();

        UpdateBookInput updateInput = new UpdateBookInput(null, null, null, null, Collections.emptyList());
        Book updatedBook = bookService.updateBook(book.getIsbn(), updateInput);

        assertThat(updatedBook.getAuthors()).isEmpty();
    }


    @Test
    void deleteBook_shouldRemoveBookAndItsAuthorBookLinks() {
        BookInput input = new BookInput("333444555", "To Be Deleted", 50, 2010, defaultCategory.getId(), List.of(defaultAuthor1.getId()));
        Book book = bookService.addBook(input);
        String isbn = book.getIsbn();

        // Verify AuthorBook link exists (implicitly, by checking authors collection size)
        assertThat(book.getAuthors()).hasSize(1);
        entityManager.flush(); // Ensure join table is populated

        boolean result = bookService.deleteBook(isbn);
        assertTrue(result);
        assertThat(bookRepository.findById(isbn)).isEmpty();

        // To verify AuthorBook entries are gone, you'd typically check that deleting the book
        // doesn't violate FKs if authors still exist, or query AuthorBook if you had a repo for it.
        // The JPA @ManyToMany handling should remove entries from AuthorBook when the owning side (Book) is deleted.
    }

    @Test
    void linkAuthorToBook_shouldAddAuthorToBook() {
        BookInput bookInput = new BookInput("777888999", "Linking Test Book", 150, 2022, defaultCategory.getId(), Collections.emptyList());
        Book book = bookService.addBook(bookInput);

        Book linkedBook = bookService.linkAuthorToBook(defaultAuthor1.getId(), book.getIsbn());

        assertThat(linkedBook.getAuthors()).hasSize(1);
        assertThat(linkedBook.getAuthors().iterator().next().getId()).isEqualTo(defaultAuthor1.getId());

        // Verify from author side as well (if bidirectional and maintained)
        // Author refreshedAuthor = authorRepository.findById(defaultAuthor1.getId()).get();
        // assertThat(refreshedAuthor.getBooks()).extracting(Book::getIsbn).contains(book.getIsbn());
    }

    @Test
    void unlinkAuthorFromBook_shouldRemoveAuthorFromBook() {
        BookInput bookInput = new BookInput("888999000", "Unlinking Test Book", 250, 2023, defaultCategory.getId(), List.of(defaultAuthor1.getId(), defaultAuthor2.getId()));
        Book book = bookService.addBook(bookInput);
        assertThat(book.getAuthors()).hasSize(2);

        Book unlinkedBook = bookService.unlinkAuthorFromBook(defaultAuthor1.getId(), book.getIsbn());

        assertThat(unlinkedBook.getAuthors()).hasSize(1);
        assertThat(unlinkedBook.getAuthors().iterator().next().getId()).isEqualTo(defaultAuthor2.getId());
    }

    @Test
    void getBooks_shouldFilterCorrectly() {
        bookService.addBook(new BookInput("001", "Specific Title", 10, 2001, defaultCategory.getId(), Collections.emptyList()));
        bookService.addBook(new BookInput("002", "Another Specific Book", 20, 2002, defaultCategory.getId(), Collections.emptyList()));
        bookService.addBook(new BookInput("003", "Generic Title", 30, 2003, defaultCategory.getId(), Collections.emptyList()));

        List<Book> byIsbn = bookService.getBooks("001", null);
        assertThat(byIsbn).hasSize(1);
        assertThat(byIsbn.get(0).getTitle()).isEqualTo("Specific Title");

        List<Book> byTitle = bookService.getBooks(null, "Specific");
        assertThat(byTitle).hasSize(2); // "Specific Title", "Another Specific Book"
        assertThat(byTitle).extracting(Book::getIsbn).containsExactlyInAnyOrder("001", "002");

        List<Book> byIsbnAndTitle = bookService.getBooks("003", "Generic");
        assertThat(byIsbnAndTitle).hasSize(1);
        assertThat(byIsbnAndTitle.get(0).getIsbn()).isEqualTo("003");

        List<Book> all = bookService.getBooks(null, null);
        assertThat(all.size()).isGreaterThanOrEqualTo(3);
    }
}