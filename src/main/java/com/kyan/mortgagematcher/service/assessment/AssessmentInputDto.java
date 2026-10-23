package com.kyan.mortgagematcher.service.assessment;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.model.applicant.ApplicantType;
import com.kyan.mortgagematcher.model.property.PropertyLocation;
import com.kyan.mortgagematcher.model.property.PropertyType;

public record AssessmentInputDto(
        String caseReference,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        BigDecimal monthlyRentalIncome,
        BigDecimal annualRentalIncome,
        BigDecimal ltv,
        PropertyType propertyType,
        PropertyLocation location,
        ApplicantType applicantType,
        boolean experiencedLandlord) {
    public static AssessmentInputDto from(MortgageAssessmentRequest request) {
        BigDecimal propertyValue = request.propertyDetails().value();
        BigDecimal monthlyRentalIncome = request.propertyDetails().expectedMonthlyRentalIncome();

        BigDecimal ltv = request.loanAmount()
                .divide(propertyValue, 4, RoundingMode.HALF_UP);

        return new AssessmentInputDto(
                request.caseReference(),
                request.loanAmount(),
                propertyValue,
                monthlyRentalIncome,
                monthlyRentalIncome.multiply(BigDecimal.valueOf(12)),
                ltv,
                request.propertyDetails().type(),
                request.propertyDetails().location(),
                request.applicantDetails().type(),
                request.applicantDetails().isExperienced());
    }
}