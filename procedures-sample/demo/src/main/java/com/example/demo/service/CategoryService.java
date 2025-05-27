package com.example.demo.service;

import com.example.demo.graphql.CategoryInput;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository; // For handling ON DELETE SET NULL behavior

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category addCategory(CategoryInput input) {
        Category category = new Category();
        category.setCategory(input.category());
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer id, CategoryInput input) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        category.setCategory(input.category());
        return categoryRepository.save(category);
    }

    @Transactional
    public boolean deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        // Handle FK_Books_Categories ON DELETE SET NULL behavior:
        // Books associated with this category should have their category field set to null.
        // The database constraint handles this automatically if we just delete the category.
        // If we wanted to do it in JPA explicitly:
        // category.getBooks().forEach(book -> book.setCategory(null));
        // bookRepository.saveAll(category.getBooks());

        categoryRepository.deleteById(id);
        return true; // Assuming deleteById throws an error if not found or handled by exists check
    }
}