package com.example.demo.graphql;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CategoryInput(
    @NotEmpty @Size(max = 64) String category
) {}