package com.kyan.mortgagematcher.api.dto;

import java.math.BigDecimal;

import com.kyan.mortgagematcher.model.applicant.ApplicantDetails;
import com.kyan.mortgagematcher.model.property.PropertyDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MortgageAssessmentRequest(
        @NotBlank String caseReference,

        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal loanAmount,

        @NotNull @Valid PropertyDetails propertyDetails,

        @NotNull @Valid ApplicantDetails applicantDetails) {
}
