package com.example.demo.service;

import com.example.demo.graphql.AuthorInput;
import com.example.demo.model.Author;
import com.example.demo.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional(readOnly = true)
    public List<Author> getAuthors(String firstname, String surname) {
        if (firstname != null && !firstname.trim().isEmpty() && surname != null && !surname.trim().isEmpty()) {
            return authorRepository.findByFirstnameContainingIgnoreCaseAndSurnameContainingIgnoreCase(firstname, surname);
        } else if (firstname != null && !firstname.trim().isEmpty()) {
            return authorRepository.findByFirstnameContainingIgnoreCase(firstname);
        } else if (surname != null && !surname.trim().isEmpty()) {
            return authorRepository.findBySurnameContainingIgnoreCase(surname);
        }
        return authorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Author> getAuthorById(Integer id) {
        return authorRepository.findById(id);
    }

    @Transactional
    public Author addAuthor(AuthorInput input) {
        Author author = new Author();
        author.setFirstname(input.firstname());
        author.setSurname(input.surname());
        author.setSurname2(input.surname2());
        return authorRepository.save(author);
    }

    @Transactional
    public Author updateAuthor(Integer id, AuthorInput input) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Author not found with id: " + id));
        author.setFirstname(input.firstname());
        author.setSurname(input.surname());
        author.setSurname2(input.surname2());
        return authorRepository.save(author);
    }

    @Transactional
    public boolean deleteAuthor(Integer id) {
        if (!authorRepository.existsById(id)) {
            // Or throw EntityNotFoundException if preferred for GraphQL error handling
            return false; 
        }
        // The DB constraint FK_AuthorBook_Authors ON DELETE CASCADE
        // will handle deleting entries from AuthorBook table.
        authorRepository.deleteById(id);
        return true;
    }
}