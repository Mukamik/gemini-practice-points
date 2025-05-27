package com.example.demo.graphql;

import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateBookInput(
    @Size(max = 256) String title,
    Integer pages,
    Integer year,
    Integer categoryId,
    List<Integer> authorIds // If provided, replaces the current set of authors
) {}