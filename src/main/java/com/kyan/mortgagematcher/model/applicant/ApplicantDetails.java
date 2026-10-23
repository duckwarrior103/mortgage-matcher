package com.kyan.mortgagematcher.model.applicant;

import jakarta.validation.constraints.NotNull;

public record ApplicantDetails(
        @NotNull ApplicantType type,

        // Boolean lets validation distinguish “missing” from false.
        @NotNull Boolean isExperienced) {
}
