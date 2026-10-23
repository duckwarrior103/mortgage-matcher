package com.kyan.mortgagematcher.api.dto;

import java.util.List;

public record MortgageAssessmentResponse(
        String caseReference,
        List<ProductAssessment> products) {
    public record ProductAssessment(
            String productId,
            String productName,
            boolean eligible,
            boolean available,
            List<String> reasons,
            List<FunderAssessment> funders) {

        public ProductAssessment {
            reasons = reasons == null ? List.of() : List.copyOf(reasons);
            funders = funders == null ? List.of() : List.copyOf(funders);
        }
    }

    public record FunderAssessment(
            String funderId,
            String funderName,
            boolean available,
            List<String> reasons) {
    }
}