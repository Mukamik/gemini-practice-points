package com.example.demo.graphql;

import jakarta.validation.constraints.NotEmpty;

// Using record for concise DTO
public record AuthorInput(
    @NotEmpty String firstname,
    @NotEmpty String surname,
    String surname2
) {}