package com.kyan.mortgagematcher.service.assessment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.kyan.mortgagematcher.model.funder.FundingPolicy;

final class FunderEligibilityEvaluator {

    static List<String> rejectionReasons(
            FundingPolicy policy,
            AssessmentInputDto input) {
        List<String> reasons = new ArrayList<>();

        checkLocation(policy, input, reasons);
        checkLtv(policy, input, reasons);
        checkExperience(policy, input, reasons);

        return reasons;
    }

    private static void checkLocation(
            FundingPolicy policy,
            AssessmentInputDto input,
            List<String> reasons) {
        if (!policy.allowedLocations().contains(input.location())) {
            reasons.add("Funder does not support this property location");
        }
    }

    private static void checkLtv(
            FundingPolicy policy,
            AssessmentInputDto input,
            List<String> reasons) {
        if (exceedsMaxLtv(input, policy.maxLtv())) {
            reasons.add("LTV exceeds the funder's maximum");
        }
    }

    private static void checkExperience(
            FundingPolicy policy,
            AssessmentInputDto input,
            List<String> reasons) {
        if (policy.landlordExperienceRequired() && !input.experiencedLandlord()) {
            reasons.add("Funder requires previous landlord experience");
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
