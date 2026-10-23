package com.kyan.mortgagematcher.service;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.api.dto.MortgageAssessmentResponse;
import com.kyan.mortgagematcher.model.applicant.ApplicantType;
import com.kyan.mortgagematcher.model.funder.Funder;
import com.kyan.mortgagematcher.model.funder.FundingPolicy;
import com.kyan.mortgagematcher.model.product.MortgageProduct;
import com.kyan.mortgagematcher.model.product.ProductCriteria;
import com.kyan.mortgagematcher.model.property.PropertyLocation;
import com.kyan.mortgagematcher.model.property.PropertyType;
import com.kyan.mortgagematcher.repository.funder.FunderRepository;
import com.kyan.mortgagematcher.repository.product.ProductRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MortgageAssessmentService {

    private final ProductRepository productRepository;
    private final FunderRepository funderRepository;

    public MortgageAssessmentService(
            ProductRepository productRepository,
            FunderRepository funderRepository) {
        this.productRepository = productRepository;
        this.funderRepository = funderRepository;
    }

    public MortgageAssessmentResponse assess(MortgageAssessmentRequest request) {
        AssessmentInputDto input = AssessmentInputDto.from(request);
        return assessAgainstCatalogue(input);
    }

    private MortgageAssessmentResponse assessAgainstCatalogue(AssessmentInputDto input) {
        List<MortgageProduct> products = productRepository.findAll();
        List<Funder> funders = funderRepository.findAll();

        List<MortgageAssessmentResponse.ProductAssessment> productAssessments = products.stream()
                .map(product -> assessProduct(product, funders, input))
                .toList();

        return new MortgageAssessmentResponse(
                input.caseReference(),
                productAssessments);
    }

    private MortgageAssessmentResponse.ProductAssessment assessProduct(
            MortgageProduct product,
            List<Funder> funders,
            AssessmentInputDto input) {
        List<String> reasons = ProductEligibilityEvaluator.rejectionReasons(product.criteria(), input);

        // Capture product eligibility before adding any funding-related reason.
        boolean eligible = reasons.isEmpty();

        List<MortgageAssessmentResponse.FunderAssessment> funderAssessments = eligible
                ? funders.stream()
                        .flatMap(funder -> funder.policies().stream()
                                .filter(policy -> policy.productId().equals(product.id()))
                                .map(policy -> assessFunder(funder, policy, input)))
                        .toList()
                : List.of();

        boolean available = eligible
                && funderAssessments.stream()
                        .anyMatch(MortgageAssessmentResponse.FunderAssessment::available);

        if (eligible && funderAssessments.isEmpty()) {
            reasons.add("No funder has a funding policy for this product");
        }

        return new MortgageAssessmentResponse.ProductAssessment(
                product.id(),
                product.name(),
                eligible,
                available,
                reasons,
                funderAssessments);
    }

    private MortgageAssessmentResponse.FunderAssessment assessFunder(
            Funder funder,
            FundingPolicy policy,
            AssessmentInputDto input) {
        List<String> reasons = FunderEligibilityEvaluator.rejectionReasons(policy, input);

        return new MortgageAssessmentResponse.FunderAssessment(
                funder.id(),
                funder.name(),
                reasons.isEmpty(),
                reasons);
    }

    private static boolean exceedsMaxLtv(
            AssessmentInputDto input,
            BigDecimal maxLtv) {
        // Compare loanAmount / propertyValue with maxLtv without rounding.
        return input.loanAmount()
                .compareTo(input.propertyValue().multiply(maxLtv)) > 0;
    }

    private static final class ProductEligibilityEvaluator {

        private static List<String> rejectionReasons(
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
    }

    private static final class FunderEligibilityEvaluator {

        private static List<String> rejectionReasons(
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
    }

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
}