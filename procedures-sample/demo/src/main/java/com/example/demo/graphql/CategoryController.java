package com.example.demo.graphql;

import com.example.demo.model.Book;
import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Set;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @QueryMapping
    public List<Category> categories() {
        return categoryService.getAllCategories();
    }

    @QueryMapping
    public Category category(@Argument Integer id) {
        return categoryService.getCategoryById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    @MutationMapping
    public Category addCategory(@Argument CategoryInput category) {
        return categoryService.addCategory(category);
    }

    @MutationMapping
    public Category updateCategory(@Argument Integer id, @Argument CategoryInput category) {
        return categoryService.updateCategory(id, category);
    }

    @MutationMapping
    public boolean deleteCategory(@Argument Integer id) {
        return categoryService.deleteCategory(id);
    }

    @SchemaMapping(typeName = "Category", field = "books")
    public Set<Book> getBooks(Category category) {
        return category.getBooks(); // Assumes lazy loading works or books are fetched
    }
}