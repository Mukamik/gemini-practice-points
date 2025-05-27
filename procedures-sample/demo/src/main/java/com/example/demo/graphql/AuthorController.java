package com.example.demo.graphql;

import com.example.demo.model.Author;
import com.example.demo.model.Book;
import com.example.demo.service.AuthorService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @QueryMapping
    public List<Author> authors(@Argument String firstname, @Argument String surname) {
        return authorService.getAuthors(firstname, surname);
    }

    @QueryMapping
    public Author author(@Argument Integer id) {
        return authorService.getAuthorById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
    }

    @MutationMapping
    public Author addAuthor(@Argument AuthorInput author) {
        return authorService.addAuthor(author);
    }

    @MutationMapping
    public Author updateAuthor(@Argument Integer id, @Argument AuthorInput author) {
        return authorService.updateAuthor(id, author);
    }

    @MutationMapping
    public boolean deleteAuthor(@Argument Integer id) {
        return authorService.deleteAuthor(id);
    }

    @SchemaMapping(typeName = "Author", field = "books")
    public Set<Book> getBooks(Author author) {
        // This relies on JPA fetching. If lazy, ensure transactional context or fetch explicitly.
        // For simplicity, assume Author entity's books collection is populated when Author is fetched,
        // or that subsequent lazy loading works within the GraphQL request transaction.
        return author.getBooks();
    }
}