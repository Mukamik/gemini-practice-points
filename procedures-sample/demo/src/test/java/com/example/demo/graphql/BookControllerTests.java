package com.example.demo.graphql;

import com.example.demo.model.Author;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@GraphQlTest(BookController.class)
public class BookControllerTests {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private BookService bookService;

    private Book book1;
    private Author author1;
    private Category category1;

    @BeforeEach
    void setUp() {
        author1 = new Author("Test", "Author", null);
        author1.setId(1);

        category1 = new Category("Fiction");
        category1.setId(1);

        book1 = new Book("1234567890123", "Test Book", 300, 2023);
        book1.setCategory(category1);
        book1.setAuthors(Set.of(author1));
    }

    @Test
    void addBookShouldReturnBook() {
        BookInput input = new BookInput("123", "New Book", 100, 2024, 1, List.of(1));
        when(bookService.addBook(any(BookInput.class))).thenReturn(book1); // Simplified return

        this.graphQlTester.documentName("bookMutations")
                .variable("input", input)
                .operationName("addBookOp")
                .execute()
                .path("addBook.isbn").entity(String.class).isEqualTo("1234567890123")
                .path("addBook.title").entity(String.class).isEqualTo("Test Book");
    }

    @Test
    void updateBookShouldReturnUpdatedBook() {
        String isbn = "1234567890123";
        UpdateBookInput input = new UpdateBookInput("Updated Title", 350, 2023, 1, List.of(1));
        Book updatedBook = new Book(isbn, "Updated Title", 350, 2023);
        updatedBook.setCategory(category1);
        updatedBook.setAuthors(Set.of(author1));

        when(bookService.updateBook(eq(isbn), any(UpdateBookInput.class))).thenReturn(updatedBook);

        this.graphQlTester.documentName("bookMutations")
                .variable("isbn", isbn)
                .variable("input", input)
                .operationName("updateBookOp")
                .execute()
                .path("updateBook.title").entity(String.class).isEqualTo("Updated Title")
                .path("updateBook.pages").entity(Integer.class).isEqualTo(350);
    }

    @Test
    void deleteBookShouldReturnTrue() {
        String isbn = "1234567890123";
        when(bookService.deleteBook(isbn)).thenReturn(true);

        this.graphQlTester.documentName("bookMutations")
                .variable("isbn", isbn)
                .operationName("deleteBookOp")
                .execute()
                .path("deleteBook").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void getBookByIsbnShouldReturnBook() {
        when(bookService.getBookByIsbn(book1.getIsbn())).thenReturn(Optional.of(book1));

        this.graphQlTester.documentName("bookQueries")
                .variable("isbn", book1.getIsbn())
                .operationName("getBookByIsbnOp")
                .execute()
                .path("book.isbn").entity(String.class).isEqualTo(book1.getIsbn())
                .path("book.title").entity(String.class).isEqualTo(book1.getTitle())
                .path("book.category.category").entity(String.class).isEqualTo("Fiction")
                .path("book.authors[0].firstname").entity(String.class).isEqualTo("Test");
    }

    @Test
    void getBooksShouldReturnListOfBooks() {
        when(bookService.getBooks(null, "Test")).thenReturn(List.of(book1));
        String query = """
            query getBooksByTitle($title: String) {
              books(title: $title) {
                isbn
                title
              }
            }
        """;
        this.graphQlTester.document(query)
                .variable("title", "Test")
                .execute()
                .path("books").entityList(Book.class).hasSize(1)
                .path("books[0].title").entity(String.class).isEqualTo("Test Book");
    }

    @Test
    void linkAuthorToBookShouldWork() {
        when(bookService.linkAuthorToBook(eq(1), eq(book1.getIsbn()))).thenReturn(book1);
         this.graphQlTester.documentName("bookMutations")
                .variable("authorId", 1)
                .variable("isbn", book1.getIsbn())
                .operationName("linkAuthorToBookOp")
                .execute()
                .path("linkAuthorToBook.isbn").entity(String.class).isEqualTo(book1.getIsbn());
    }

    @Test
    void unlinkAuthorFromBookShouldWork() {
         // Simulate book after unlinking (author removed)
        Book bookAfterUnlink = new Book(book1.getIsbn(), book1.getTitle(), book1.getPages(), book1.getYear());
        bookAfterUnlink.setCategory(book1.getCategory());
        // authors set is now empty

        when(bookService.unlinkAuthorFromBook(eq(1), eq(book1.getIsbn()))).thenReturn(bookAfterUnlink);

         this.graphQlTester.documentName("bookMutations")
                .variable("authorId", 1)
                .variable("isbn", book1.getIsbn())
                .operationName("unlinkAuthorFromBookOp")
                .execute()
                .path("unlinkAuthorFromBook.isbn").entity(String.class).isEqualTo(book1.getIsbn())
                .path("unlinkAuthorFromBook.authors").entityList(Object.class).hasSize(0); // Verify authors list is empty
    }
}