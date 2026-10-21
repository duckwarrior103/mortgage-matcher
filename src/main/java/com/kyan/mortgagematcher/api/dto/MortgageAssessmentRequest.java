package com.kyan.mortgagematcher.api.dto;

import java.math.BigDecimal;

import com.kyan.mortgagematcher.model.ApplicantDetails;
import com.kyan.mortgagematcher.model.PropertyDetails;

public record MortgageAssessmentRequest(
        String caseReference,
        BigDecimal loanAmount,
        PropertyDetails propertyDetails,
        ApplicantDetails applicantDetails) {
}
