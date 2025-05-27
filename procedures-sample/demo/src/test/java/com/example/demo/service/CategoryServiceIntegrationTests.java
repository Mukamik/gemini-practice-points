package com.example.demo.service;

import com.example.demo.graphql.CategoryInput;
import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class CategoryServiceIntegrationTests {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void addCategory_shouldSaveAndReturnCategory() {
        CategoryInput input = new CategoryInput("Science Fiction");
        Category savedCategory = categoryService.addCategory(input);

        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getCategory()).isEqualTo("Science Fiction");

        Optional<Category> found = categoryRepository.findById(savedCategory.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCategory()).isEqualTo("Science Fiction");
    }

    @Test
    void addCategory_withDuplicateName_shouldThrowException() {
        categoryService.addCategory(new CategoryInput("Unique Category"));
        CategoryInput duplicateInput = new CategoryInput("Unique Category");

        // Expecting a DataIntegrityViolationException due to unique constraint on category name
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryService.addCategory(duplicateInput);
        });
    }

    @Test
    void updateCategory_shouldUpdateExistingCategory() {
        Category initialCategory = categoryService.addCategory(new CategoryInput("Fantasy"));
        CategoryInput updateInput = new CategoryInput("High Fantasy");

        Category updatedCategory = categoryService.updateCategory(initialCategory.getId(), updateInput);

        assertThat(updatedCategory.getCategory()).isEqualTo("High Fantasy");
        Optional<Category> found = categoryRepository.findById(initialCategory.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCategory()).isEqualTo("High Fantasy");
    }

    @Test
    void deleteCategory_shouldRemoveCategoryAndSetNullInBooks() {
        Category category = categoryService.addCategory(new CategoryInput("Mystery"));
        Book book = new Book("5556667778889", "The Hound of the Baskervilles", 250, 1902);
        book.setCategory(category);
        bookRepository.save(book);
        entityManager.flush(); // Ensure book is persisted with category link

        assertTrue(categoryService.deleteCategory(category.getId()));
        assertThat(categoryRepository.findById(category.getId())).isEmpty();

        // Verify FK_Books_Categories ON DELETE SET NULL behavior
        entityManager.clear(); // Clear persistence context to force reload from DB
        Book updatedBook = bookRepository.findById(book.getIsbn()).orElse(null);
        assertThat(updatedBook).isNotNull();
        assertThat(updatedBook.getCategory()).isNull();
    }

    @Test
    void getAllCategories_shouldReturnAllCategories() {
        categoryService.addCategory(new CategoryInput("Horror"));
        categoryService.addCategory(new CategoryInput("Thriller"));

        List<Category> categories = categoryService.getAllCategories();
        assertThat(categories.size()).isGreaterThanOrEqualTo(2);
        assertThat(categories).extracting(Category::getCategory).contains("Horror", "Thriller");
    }
}