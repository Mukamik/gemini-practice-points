package com.example.demo.graphql;

import com.example.demo.model.Category;
import com.example.demo.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@GraphQlTest(CategoryController.class)
public class CategoryControllerTests {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockBean
    private CategoryService categoryService;

    @Test
    void addCategoryShouldReturnCategory() {
        CategoryInput input = new CategoryInput("Sci-Fi");
        Category expectedCategory = new Category("Sci-Fi");
        expectedCategory.setId(1);

        when(categoryService.addCategory(any(CategoryInput.class))).thenReturn(expectedCategory);

        this.graphQlTester.documentName("categoryMutations")
                .variable("input", input)
                .operationName("addCategoryOp")
                .execute()
                .path("addCategory.id").entity(Integer.class).isEqualTo(1)
                .path("addCategory.category").entity(String.class).isEqualTo("Sci-Fi");
    }

    @Test
    void updateCategoryShouldReturnUpdatedCategory() {
        Integer categoryId = 1;
        CategoryInput input = new CategoryInput("Science Fiction");
        Category updatedCategory = new Category("Science Fiction");
        updatedCategory.setId(categoryId);

        when(categoryService.updateCategory(eq(categoryId), any(CategoryInput.class))).thenReturn(updatedCategory);

        this.graphQlTester.documentName("categoryMutations")
                .variable("id", categoryId)
                .variable("input", input)
                .operationName("updateCategoryOp")
                .execute()
                .path("updateCategory.id").entity(Integer.class).isEqualTo(categoryId)
                .path("updateCategory.category").entity(String.class).isEqualTo("Science Fiction");
    }

    @Test
    void deleteCategoryShouldReturnTrue() {
        Integer categoryId = 1;
        when(categoryService.deleteCategory(categoryId)).thenReturn(true);

        this.graphQlTester.documentName("categoryMutations")
                .variable("id", categoryId)
                .operationName("deleteCategoryOp")
                .execute()
                .path("deleteCategory").entity(Boolean.class).isEqualTo(true);
    }

    @Test
    void getCategoryByIdShouldReturnCategory() {
        Integer categoryId = 1;
        Category expectedCategory = new Category("Fiction");
        expectedCategory.setId(categoryId);

        when(categoryService.getCategoryById(categoryId)).thenReturn(Optional.of(expectedCategory));

        this.graphQlTester.documentName("categoryQueries")
                .variable("id", categoryId)
                .operationName("getCategoryByIdOp")
                .execute()
                .path("category.id").entity(Integer.class).isEqualTo(categoryId)
                .path("category.category").entity(String.class).isEqualTo("Fiction");
    }

    @Test
    void getCategoriesShouldReturnListOfCategories() {
        Category cat1 = new Category("Fiction"); cat1.setId(1);
        Category cat2 = new Category("Non-Fiction"); cat2.setId(2);

        when(categoryService.getAllCategories()).thenReturn(List.of(cat1, cat2));

        String query = """
            query getCategoriesOp {
              categories {
                id
                category
              }
            }
        """;
        this.graphQlTester.document(query)
                .execute()
                .path("categories").entityList(Category.class).hasSize(2)
                .path("categories[0].category").entity(String.class).isEqualTo("Fiction");
    }
}