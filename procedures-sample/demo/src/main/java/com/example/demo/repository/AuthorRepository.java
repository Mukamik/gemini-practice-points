package com.example.demo.repository;

import com.example.demo.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
    // Corresponds to usp_get_authors_storebook filtering
    List<Author> findByFirstnameContainingIgnoreCaseAndSurnameContainingIgnoreCase(String firstname, String surname);
    List<Author> findByFirstnameContainingIgnoreCase(String firstname);
    List<Author> findBySurnameContainingIgnoreCase(String surname);
}