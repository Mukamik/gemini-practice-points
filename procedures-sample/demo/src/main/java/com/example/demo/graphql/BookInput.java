package com.example.demo.graphql;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BookInput(
    @NotEmpty @Size(max = 13) String isbn,
    @NotEmpty @Size(max = 256) String title,
    Integer pages,
    Integer year,
    Integer categoryId, // ID of an existing category
    List<Integer> authorIds // List of IDs of existing authors
) {}