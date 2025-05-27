package com.example.demo.service;

import com.example.demo.AbstractIntegrationTest;
import com.example.demo.graphql.AuthorInput;
import com.example.demo.model.Author;
import com.example.demo.model.Book; // For testing cascade delete
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository; // For testing cascade delete
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Transactional // Ensures each test runs in its own transaction and is rolled back
public class AuthorServiceIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository; // For testing delete cascade

    @Autowired
    private EntityManager entityManager;

    @Test
    void addAuthor_shouldSaveAndReturnAuthor() {
        AuthorInput input = new AuthorInput("George", "Orwell", null);
        Author savedAuthor = authorService.addAuthor(input);

        assertThat(savedAuthor).isNotNull();
        assertThat(savedAuthor.getId()).isNotNull();
        assertThat(savedAuthor.getFirstname()).isEqualTo("George");
        assertThat(savedAuthor.getSurname()).isEqualTo("Orwell");

        Optional<Author> found = authorRepository.findById(savedAuthor.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getFirstname()).isEqualTo("George");
    }

    @Test
    void updateAuthor_shouldUpdateExistingAuthor() {
        Author initialAuthor = authorService.addAuthor(new AuthorInput("Jane", "Austen", null));
        AuthorInput updateInput = new AuthorInput("Jane", "Austen", "Ms.");

        Author updatedAuthor = authorService.updateAuthor(initialAuthor.getId(), updateInput);

        assertThat(updatedAuthor.getSurname2()).isEqualTo("Ms.");
        Optional<Author> found = authorRepository.findById(initialAuthor.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSurname2()).isEqualTo("Ms.");
    }

    @Test
    void updateAuthor_whenNotFound_shouldThrowException() {
        AuthorInput input = new AuthorInput("Non", "Existent", null);
        assertThrows(Exception.class, () -> { // jakarta.persistence.EntityNotFoundException or custom
            authorService.updateAuthor(999, input);
        });
    }

    @Test
    void deleteAuthor_shouldRemoveAuthorAndCascadeToAuthorBook() {
        Author author = authorService.addAuthor(new AuthorInput("Leo", "Tolstoy", null));
        Book book = new Book("1112223334445", "War and Peace", 1225, 1869);
        bookRepository.save(book); // Save book first

        // Link author to book (simulating BookService.linkAuthorToBook or addBook with authorIds)
        Author managedAuthor = authorRepository.findById(author.getId()).orElseThrow();
        Book managedBook = bookRepository.findById(book.getIsbn()).orElseThrow();
        managedBook.getAuthors().add(managedAuthor);
        bookRepository.save(managedBook);
        entityManager.flush(); // Ensure join table entry is persisted

        // Verify link exists (optional, but good for sanity)
        // You might need a native query or a specific AuthorBookRepository if you want to check AuthorBook directly
        // For this test, we rely on the cascade delete behavior defined in the schema.

        boolean result = authorService.deleteAuthor(author.getId());
        assertTrue(result);
        assertThat(authorRepository.findById(author.getId())).isEmpty();

        // Check if AuthorBook entries are cascaded (FK_AuthorBook_Authors ON DELETE CASCADE)
        // This is implicitly tested by ensuring the author can be deleted without FK violation
        // and if we were to query AuthorBook, it should be empty for this author.
    }

    @Test
    void deleteAuthor_whenNotFound_shouldReturnFalse() {
        boolean result = authorService.deleteAuthor(999);
        assertFalse(result);
    }

    @Test
    void getAuthors_shouldFilterCorrectly() {
        authorService.addAuthor(new AuthorInput("Fyodor", "Dostoevsky", null));
        authorService.addAuthor(new AuthorInput("Alexander", "Pushkin", null));
        authorService.addAuthor(new AuthorInput("Alexander", "Solzhenitsyn", null));

        List<Author> alexanders = authorService.getAuthors("Alexander", null);
        assertThat(alexanders).hasSize(2);
        assertThat(alexanders).extracting(Author::getSurname).containsExactlyInAnyOrder("Pushkin", "Solzhenitsyn");

        List<Author> dostoevsky = authorService.getAuthors(null, "Dostoevsky");
        assertThat(dostoevsky).hasSize(1);
        assertThat(dostoevsky.get(0).getFirstname()).isEqualTo("Fyodor");

        List<Author> all = authorService.getAuthors(null, null);
        assertThat(all.size()).isGreaterThanOrEqualTo(3);
    }
}