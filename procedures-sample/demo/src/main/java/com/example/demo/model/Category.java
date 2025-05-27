package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "Categories", uniqueConstraints = @UniqueConstraint(columnNames = "category"))
@EqualsAndHashCode(exclude = "books") // Prevent recursion
@ToString(exclude = "books") // Prevent recursion
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 64) // Original schema had 46, then 64. Using 64.
    private String category;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // If a category is deleted, books referencing it will have their category_id set to null by DB.
    // CascadeType.ALL here means if we delete a Category via JPA, it might try to delete associated books.
    // This is not what FK_Books_Categories (ON DELETE SET NULL) does.
    // So, service layer should handle this or we remove CascadeType.ALL and rely on DB.
    // For now, let's remove CascadeType.ALL and handle it in service or rely on DB.
    private Set<Book> books = new HashSet<>();

    public Category() {}

    public Category(String category) {
        this.category = category;
    }
}