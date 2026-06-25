package com.example.chat.domain.meetup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReviewRequest(

        @NotNull
        @Min(1) @Max(5)
        Integer rating,

        List<String> tags
) {}
