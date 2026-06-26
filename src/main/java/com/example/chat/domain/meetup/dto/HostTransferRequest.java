package com.example.chat.domain.meetup.dto;

import jakarta.validation.constraints.NotNull;

public record HostTransferRequest(
        @NotNull Long newHostId
) {}
