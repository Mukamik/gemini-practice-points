package com.example.demo.repository;

import com.example.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    // Corresponds to usp_get_books_storebook filtering
    List<Book> findByIsbnContainingIgnoreCaseAndTitleContainingIgnoreCase(String isbn, String title);
    List<Book> findByIsbnContainingIgnoreCase(String isbn);
    List<Book> findByTitleContainingIgnoreCase(String title);
}