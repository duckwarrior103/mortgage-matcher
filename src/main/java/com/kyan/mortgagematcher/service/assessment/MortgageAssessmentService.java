package com.kyan.mortgagematcher.service.assessment;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.api.dto.MortgageAssessmentResponse;
import com.kyan.mortgagematcher.model.funder.Funder;
import com.kyan.mortgagematcher.model.funder.FundingPolicy;
import com.kyan.mortgagematcher.model.product.MortgageProduct;
import com.kyan.mortgagematcher.repository.funder.FunderRepository;
import com.kyan.mortgagematcher.repository.product.ProductRepository;

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
                        .anyMatch(funderAssessment -> funderAssessment.available());

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

}