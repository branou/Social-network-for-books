package com.himbra.book.feedback;

import jakarta.validation.constraints.*;

public record FeedbackRequest (
        @DecimalMin(value = "0.0", inclusive = false, message = "Note must be greater than 0")
        @DecimalMax(value = "5.0", message = "Note must be less than or equal to 5")
        Double note,
        @NotNull(message = "203")
        @NotEmpty(message = "203")
        @NotBlank(message = "203")
        String comment,
        @NotNull(message = "204")
        Long bookId){}
