package com.kyan.mortgagematcher.service.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.model.applicant.ApplicantDetails;
import com.kyan.mortgagematcher.model.applicant.ApplicantType;
import com.kyan.mortgagematcher.model.product.ProductCriteria;
import com.kyan.mortgagematcher.model.property.PropertyDetails;
import com.kyan.mortgagematcher.model.property.PropertyLocation;
import com.kyan.mortgagematcher.model.property.PropertyType;

class ProductEligibilityEvaluatorTest {

    private static final ProductCriteria BASE_CRITERIA = new ProductCriteria(
            bd("100000"),
            bd("500000"),
            bd("0.75"),
            bd("0.05"),
            bd("1.25"),
            Set.of(PropertyType.STANDARD),
            Set.of(ApplicantType.INDIVIDUAL),
            Set.of(PropertyLocation.ENGLAND),
            false);

    @Test
    void eligibleApplicationHasNoRejectionReasons() {
        assertThat(reasons(BASE_CRITERIA, validInput("200000", "400000", "10000")))
                .isEmpty();
    }

    @Test
    void loanAmountMustMeetInclusiveBounds() {
        assertThat(reasons(BASE_CRITERIA, validInput("99999", "400000", "10000")))
                .containsExactly("Loan amount is below the product minimum");
        assertThat(reasons(BASE_CRITERIA, validInput("100000", "400000", "10000")))
                .isEmpty();
        assertThat(reasons(BASE_CRITERIA, validInput("500000", "1000000", "10000")))
                .isEmpty();
        assertThat(reasons(BASE_CRITERIA, validInput("500001", "1000000", "10000")))
                .containsExactly("Loan amount exceeds the product maximum");
    }

    @Test
    void ltvAtTheLimitIsAcceptedAndValuesAboveItAreRejectedWithoutRounding() {
        assertThat(reasons(BASE_CRITERIA, validInput("300000", "400000", "10000")))
                .isEmpty();
        assertThat(reasons(BASE_CRITERIA, validInput("300000.01", "400000", "10000")))
                .containsExactly("LTV exceeds the product maximum");

        ProductCriteria roundedLimitCriteria = new ProductCriteria(
                bd("50000"),
                bd("500000"),
                bd("0.6666"),
                bd("0.05"),
                bd("1.25"),
                Set.of(PropertyType.STANDARD),
                Set.of(ApplicantType.INDIVIDUAL),
                Set.of(PropertyLocation.ENGLAND),
                false);
        AssessmentInputDto roundedInput = validInput("66660.50", "100000", "10000");

        assertThat(roundedInput.ltv()).isEqualByComparingTo("0.6666");
        assertThat(reasons(roundedLimitCriteria, roundedInput))
                .containsExactly("LTV exceeds the product maximum");
    }

    @Test
    void unsupportedPropertyTypeIsRejected() {
        assertThat(reasons(BASE_CRITERIA, input(
                "200000", "400000", "10000", PropertyType.HMO,
                PropertyLocation.ENGLAND, ApplicantType.INDIVIDUAL, false)))
                .containsExactly("Property type is not supported by this product");
    }

    @Test
    void unsupportedApplicantTypeIsRejected() {
        assertThat(reasons(BASE_CRITERIA, input(
                "200000", "400000", "10000", PropertyType.STANDARD,
                PropertyLocation.ENGLAND, ApplicantType.LIMITED_COMPANY, false)))
                .containsExactly("Applicant type is not supported by this product");
    }

    @Test
    void unsupportedLocationIsRejected() {
        assertThat(reasons(BASE_CRITERIA, input(
                "200000", "400000", "10000", PropertyType.STANDARD,
                PropertyLocation.WALES, ApplicantType.INDIVIDUAL, false)))
                .containsExactly("Property location is not supported by this product");
    }

    @Test
    void requiredLandlordExperienceIsEnforced() {
        ProductCriteria experiencedLandlordCriteria = new ProductCriteria(
                BASE_CRITERIA.minLoanAmount(),
                BASE_CRITERIA.maxLoanAmount(),
                BASE_CRITERIA.maxLtv(),
                BASE_CRITERIA.annualStressRate(),
                BASE_CRITERIA.minIcr(),
                BASE_CRITERIA.allowedPropertyTypes(),
                BASE_CRITERIA.allowedApplicantTypes(),
                BASE_CRITERIA.allowedLocations(),
                true);

        assertThat(reasons(experiencedLandlordCriteria,
                validInput("200000", "400000", "10000")))
                .containsExactly("Previous landlord experience is required");
    }

    @Test
    void rentalIncomeMustMeetTheStressTestThreshold() {
        AssessmentInputDto atThreshold = validInput("192000", "400000", "1000");
        AssessmentInputDto belowThreshold = validInput("192000", "400000", "999.99");

        assertThat(atThreshold.annualRentalIncome()).isEqualByComparingTo("12000");
        assertThat(reasons(BASE_CRITERIA, atThreshold)).isEmpty();
        assertThat(reasons(BASE_CRITERIA, belowThreshold))
                .containsExactly("Rental income does not meet the product's stress test");
    }

    @Test
    void multipleFailedCriteriaProduceMultipleReasons() {
        ProductCriteria strictCriteria = new ProductCriteria(
                bd("100000"),
                bd("500000"),
                bd("0.75"),
                bd("0.05"),
                bd("1.25"),
                Set.of(PropertyType.STANDARD),
                Set.of(ApplicantType.INDIVIDUAL),
                Set.of(PropertyLocation.ENGLAND),
                true);

        assertThat(reasons(strictCriteria, input(
                "50000", "60000", "100", PropertyType.HMO,
                PropertyLocation.SCOTLAND, ApplicantType.LIMITED_COMPANY, false)))
                .containsExactly(
                        "Loan amount is below the product minimum",
                        "LTV exceeds the product maximum",
                        "Property type is not supported by this product",
                        "Applicant type is not supported by this product",
                        "Property location is not supported by this product",
                        "Previous landlord experience is required",
                        "Rental income does not meet the product's stress test");
    }

    private static AssessmentInputDto validInput(
            String loanAmount,
            String propertyValue,
            String monthlyRentalIncome) {
        return input(
                loanAmount,
                propertyValue,
                monthlyRentalIncome,
                PropertyType.STANDARD,
                PropertyLocation.ENGLAND,
                ApplicantType.INDIVIDUAL,
                false);
    }

    private static AssessmentInputDto input(
            String loanAmount,
            String propertyValue,
            String monthlyRentalIncome,
            PropertyType propertyType,
            PropertyLocation location,
            ApplicantType applicantType,
            boolean experiencedLandlord) {
        return AssessmentInputDto.from(new MortgageAssessmentRequest(
                "CASE-001",
                bd(loanAmount),
                new PropertyDetails(
                        bd(propertyValue),
                        bd(monthlyRentalIncome),
                        propertyType,
                        location),
                new ApplicantDetails(applicantType, experiencedLandlord)));
    }

    private static java.util.List<String> reasons(
            ProductCriteria criteria,
            AssessmentInputDto input) {
        return ProductEligibilityEvaluator.rejectionReasons(criteria, input);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
