package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "Authors")
@EqualsAndHashCode(exclude = "books") // Prevent recursion in hashCode/equals
@ToString(exclude = "books") // Prevent recursion in toString
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 128)
    private String firstname;

    @Column(nullable = false, length = 128)
    private String surname;

    @Column(length = 128)
    private String surname2;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public Author() {}

    public Author(String firstname, String surname, String surname2) {
        this.firstname = firstname;
        this.surname = surname;
        this.surname2 = surname2;
    }
}