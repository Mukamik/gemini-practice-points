package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "Books")
@EqualsAndHashCode(exclude = "authors") // Prevent recursion
@ToString(exclude = "authors") // Prevent recursion
public class Book {
    @Id
    @Column(length = 13)
    private String isbn;

    @Column(nullable = false, length = 256)
    private String title;

    private Integer pages;
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId") // FK_Books_Categories ON DELETE SET NULL
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "AuthorBook",
            joinColumns = @JoinColumn(name = "Isbn"), // FK to Books.Isbn
            inverseJoinColumns = @JoinColumn(name = "IdAuthor") // FK to Authors.Id
            // The AuthorBook table in createScript.sql also has a 'Created' datetime column.
            // Standard @ManyToMany doesn't easily support extra columns in the join table.
            // If 'Created' is critical for logic/querying (it's not in the provided SPs beyond insert),
            // an intermediate entity AuthorBookLink would be needed.
            // For this conversion, we'll assume 'Created' is an audit field not directly managed by these GraphQL ops.
            // DB constraint FK_AuthorBook_Authors has ON DELETE CASCADE.
            // DB constraint FK_AuthorBook_Books has ON DELETE NO ACTION.
            // JPA will handle the join table entries when a Book (owner of relationship) is deleted.
    )
    private Set<Author> authors = new HashSet<>();

    public Book() {}

    public Book(String isbn, String title, Integer pages, Integer year) {
        this.isbn = isbn;
        this.title = title;
        this.pages = pages;
        this.year = year;
    }
}