package com.example.demo.graphql;

import com.example.demo.model.Author;
import com.example.demo.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@GraphQlTest(AuthorController.class)
public class AuthorControllerTests {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private AuthorService authorService;

    @Test
    void addAuthorShouldReturnAuthor() {
        AuthorInput input = new AuthorInput("John", "Doe", null);
        Author expectedAuthor = new Author("John", "Doe", null);
        expectedAuthor.setId(1);

        when(authorService.addAuthor(any(AuthorInput.class))).thenReturn(expectedAuthor);

        String document = """
            mutation addAuthorOp($input: AuthorInput!) {
              addAuthor(author: $input) {
                id
                firstname
                surname
                surname2
              }
            }
        """;
        this.graphQlTester.document(document)
                .variable("input", input)
                .execute()
                .path("addAuthor.id").entity(Integer.class).isEqualTo(1)
                .path("addAuthor.firstname").entity(String.class).isEqualTo("John")
                .path("addAuthor.surname").entity(String.class).isEqualTo("Doe");
    }

    @Test
    void updateAuthorShouldReturnUpdatedAuthor() {
        Integer authorId = 1;
        AuthorInput input = new AuthorInput("Jane", "Doe", "Smith");
        Author updatedAuthor = new Author("Jane", "Doe", "Smith");
        updatedAuthor.setId(authorId);

        when(authorService.updateAuthor(eq(authorId), any(AuthorInput.class))).thenReturn(updatedAuthor);

        String document = """
            mutation updateAuthorOp($id: ID!, $input: AuthorInput!) {
              updateAuthor(id: $id, author: $input) {
                id
                firstname
                surname
                surname2
              }
            }
        """;
        this.graphQlTester.document(document)
                .variable("id", authorId)
                .variable("input", input)
                .execute()
                .path("updateAuthor.id").entity(Integer.class).isEqualTo(authorId)
                .path("updateAuthor.firstname").entity(String.class).isEqualTo("Jane")
                .path("updateAuthor.surname2").entity(String.class).isEqualTo("Smith");
    }

    @Test
    void deleteAuthorShouldReturnTrue() {
        Integer authorId = 1;
        when(authorService.deleteAuthor(authorId)).thenReturn(true);

        String document = """
            mutation deleteAuthorOp($id: ID!) {
              deleteAuthor(id: $id)
            }
        """;
        this.graphQlTester.document(document)
                .variable("id", authorId)
                .execute()
                .path("deleteAuthor").entity(Boolean.class).isEqualTo(true);

        verify(authorService).deleteAuthor(authorId);
    }

    @Test
    void getAuthorByIdShouldReturnAuthor() {
        Integer authorId = 1;
        Author expectedAuthor = new Author("John", "Doe", null);
        expectedAuthor.setId(authorId);

        when(authorService.getAuthorById(authorId)).thenReturn(Optional.of(expectedAuthor));

        String document = """
            query getAuthorByIdOp($id: ID!) {
              author(id: $id) {
                id
                firstname
                surname
                surname2
              }
            }
        """;
        this.graphQlTester.document(document)
                .variable("id", authorId)
                .execute()
                .path("author.id").entity(Integer.class).isEqualTo(authorId)
                .path("author.firstname").entity(String.class).isEqualTo("John");
    }

    @Test
    void getAuthorsShouldReturnListOfAuthors() {
        Author author1 = new Author("John", "Doe", null);
        author1.setId(1);
        Author author2 = new Author("Jane", "Doe", null);
        author2.setId(2);

        // Corrected mock call to match controller behavior for surname filter
        when(authorService.getAuthors(eq(null), eq("Doe"))).thenReturn(List.of(author1, author2));

        // Embedded the query directly
        String document = """
            query getAuthorsBySurname($surname: String) {
              authors(surname: $surname) {
                id
                firstname
                surname
              }
            }
        """;

        this.graphQlTester.document(document)
                .variable("surname", "Doe")
                .execute()
                .path("authors").entityList(Author.class).hasSize(2)
                .path("authors[0].firstname").entity(String.class).isEqualTo("John");
    }

     @Test
    void getAuthorByIdNotFound() {
        Integer authorId = 99;
        when(authorService.getAuthorById(authorId)).thenReturn(Optional.empty());

        // This test expects an EntityNotFoundException to be thrown by the controller
        // and handled by GraphQLExceptionHandler (if configured) or default Spring GraphQL error handling.
        String document = """
            query getAuthorByIdOp($id: ID!) {
              author(id: $id) {
                id
                firstname
                surname
                surname2
              }
            }
        """;
        this.graphQlTester.document(document)
                .variable("id", authorId)
                .execute()
                .errors().expect(err -> err.getMessage() != null && err.getMessage().contains("Author not found with id: " + authorId)
                // && err.getErrorType() == ErrorType.NOT_FOUND // If custom handler is active
                );
    }
}