package com.kyan.mortgagematcher.service.assessment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.kyan.mortgagematcher.model.product.ProductCriteria;

final class ProductEligibilityEvaluator {

    static List<String> rejectionReasons(
            ProductCriteria criteria,
            AssessmentInputDto input) {
        List<String> reasons = new ArrayList<>();

        checkLoanAmount(criteria, input, reasons);
        checkLtv(criteria, input, reasons);
        checkPropertyType(criteria, input, reasons);
        checkApplicantType(criteria, input, reasons);
        checkLocation(criteria, input, reasons);
        checkExperience(criteria, input, reasons);
        checkRentalIncome(criteria, input, reasons);

        return reasons;
    }

    private static void checkLoanAmount(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        if (input.loanAmount().compareTo(criteria.minLoanAmount()) < 0) {
            reasons.add("Loan amount is below the product minimum");
        }

        if (input.loanAmount().compareTo(criteria.maxLoanAmount()) > 0) {
            reasons.add("Loan amount exceeds the product maximum");
        }
    }

    private static void checkLtv(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        if (exceedsMaxLtv(input, criteria.maxLtv())) {
            reasons.add("LTV exceeds the product maximum");
        }
    }

    private static void checkPropertyType(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        if (!criteria.allowedPropertyTypes().contains(input.propertyType())) {
            reasons.add("Property type is not supported by this product");
        }
    }

    private static void checkApplicantType(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        if (!criteria.allowedApplicantTypes().contains(input.applicantType())) {
            reasons.add("Applicant type is not supported by this product");
        }
    }

    private static void checkLocation(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        if (!criteria.allowedLocations().contains(input.location())) {
            reasons.add("Property location is not supported by this product");
        }
    }

    private static void checkExperience(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        if (criteria.landlordExperienceRequired() && !input.experiencedLandlord()) {
            reasons.add("Previous landlord experience is required");
        }
    }

    private static void checkRentalIncome(
            ProductCriteria criteria,
            AssessmentInputDto input,
            List<String> reasons) {
        BigDecimal requiredAnnualRent = input.loanAmount()
                .multiply(criteria.annualStressRate())
                .multiply(criteria.minIcr());

        if (input.annualRentalIncome().compareTo(requiredAnnualRent) < 0) {
            reasons.add("Rental income does not meet the product's stress test");
        }
    }

    private static boolean exceedsMaxLtv(
            AssessmentInputDto input,
            BigDecimal maxLtv) {
        // Compare loanAmount / propertyValue with maxLtv without rounding.
        return input.loanAmount()
                .compareTo(input.propertyValue().multiply(maxLtv)) > 0;
    }
}
