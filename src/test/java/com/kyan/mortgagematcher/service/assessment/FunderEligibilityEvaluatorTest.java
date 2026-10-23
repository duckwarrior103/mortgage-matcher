package com.kyan.mortgagematcher.service.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.model.applicant.ApplicantDetails;
import com.kyan.mortgagematcher.model.applicant.ApplicantType;
import com.kyan.mortgagematcher.model.funder.FundingPolicy;
import com.kyan.mortgagematcher.model.property.PropertyDetails;
import com.kyan.mortgagematcher.model.property.PropertyLocation;
import com.kyan.mortgagematcher.model.property.PropertyType;

class FunderEligibilityEvaluatorTest {

        @Test
        void acceptingPolicyProducesNoRejectionReasons() {
                assertThat(reasons(policy(Set.of(PropertyLocation.ENGLAND), "0.75", false),
                                input("300000", "400000", PropertyLocation.ENGLAND, false)))
                                .isEmpty();
        }

        @Test
        void unsupportedLocationIsRejected() {
                assertThat(reasons(policy(Set.of(PropertyLocation.ENGLAND), "0.75", false),
                                input("300000", "400000", PropertyLocation.SCOTLAND, false)))
                                .containsExactly("Funder does not support this property location");
        }

        @Test
        void ltvAtThePolicyLimitIsAcceptedAndValuesAboveItAreRejected() {
                FundingPolicy policy = policy(Set.of(PropertyLocation.ENGLAND), "0.75", false);

                assertThat(reasons(policy, input("300000", "400000", PropertyLocation.ENGLAND, false)))
                                .isEmpty();
                assertThat(reasons(policy, input("300000.01", "400000", PropertyLocation.ENGLAND, false)))
                                .containsExactly("LTV exceeds the funder's maximum");
        }

        @Test
        void requiredLandlordExperienceIsEnforced() {
                assertThat(reasons(policy(Set.of(PropertyLocation.ENGLAND), "0.75", true),
                                input("300000", "400000", PropertyLocation.ENGLAND, false)))
                                .containsExactly("Funder requires previous landlord experience");
        }

        @Test
        void multipleFailedPolicyChecksProduceMultipleReasons() {
                assertThat(reasons(policy(Set.of(PropertyLocation.ENGLAND), "0.65", true),
                                input("300000", "400000", PropertyLocation.SCOTLAND, false)))
                                .containsExactly(
                                                "Funder does not support this property location",
                                                "LTV exceeds the funder's maximum",
                                                "Funder requires previous landlord experience");
        }

        private static FundingPolicy policy(
                        Set<PropertyLocation> allowedLocations,
                        String maxLtv,
                        boolean landlordExperienceRequired) {
                return new FundingPolicy(
                                "PRODUCT-1",
                                allowedLocations,
                                bd(maxLtv),
                                landlordExperienceRequired);
        }

        private static AssessmentInputDto input(
                        String loanAmount,
                        String propertyValue,
                        PropertyLocation location,
                        boolean experiencedLandlord) {
                return AssessmentInputDto.from(new MortgageAssessmentRequest(
                                "CASE-001",
                                bd(loanAmount),
                                new PropertyDetails(
                                                bd(propertyValue),
                                                bd("10000"),
                                                PropertyType.STANDARD,
                                                location),
                                new ApplicantDetails(ApplicantType.INDIVIDUAL, experiencedLandlord)));
        }

        private static java.util.List<String> reasons(
                        FundingPolicy policy,
                        AssessmentInputDto input) {
                return FunderEligibilityEvaluator.rejectionReasons(policy, input);
        }

        private static BigDecimal bd(String value) {
                return new BigDecimal(value);
        }
}
