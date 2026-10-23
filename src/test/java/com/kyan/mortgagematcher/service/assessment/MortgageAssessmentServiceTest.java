package com.kyan.mortgagematcher.service.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.api.dto.MortgageAssessmentResponse;
import com.kyan.mortgagematcher.model.applicant.ApplicantDetails;
import com.kyan.mortgagematcher.model.applicant.ApplicantType;
import com.kyan.mortgagematcher.model.funder.Funder;
import com.kyan.mortgagematcher.model.funder.FundingPolicy;
import com.kyan.mortgagematcher.model.product.MortgageProduct;
import com.kyan.mortgagematcher.model.product.ProductCriteria;
import com.kyan.mortgagematcher.model.property.PropertyDetails;
import com.kyan.mortgagematcher.model.property.PropertyLocation;
import com.kyan.mortgagematcher.model.property.PropertyType;
import com.kyan.mortgagematcher.repository.funder.FunderRepository;
import com.kyan.mortgagematcher.repository.product.ProductRepository;

class MortgageAssessmentServiceTest {

        private final ProductRepository productRepository = mock(ProductRepository.class);
        private final FunderRepository funderRepository = mock(FunderRepository.class);
        private final MortgageAssessmentService service = new MortgageAssessmentService(productRepository,
                        funderRepository);

        @Test
        void preservesCaseReferenceReturnsEveryProductAndKeepsRepositoryOrder() {
                // given
                MortgageProduct secondProduct = product("PRODUCT-2");
                MortgageProduct firstProduct = product("PRODUCT-1");
                Funder secondFunder = funder("FUNDER-2", policy("PRODUCT-2", "0.75", false));
                Funder firstFunder = funder("FUNDER-1", policy("PRODUCT-1", "0.75", false));

                given(productRepository.findAll()).willReturn(List.of(secondProduct, firstProduct));
                given(funderRepository.findAll()).willReturn(List.of(secondFunder, firstFunder));

                // when
                MortgageAssessmentResponse response = service.assess(request("CASE-123"));

                // then
                assertThat(response.caseReference()).isEqualTo("CASE-123");
                assertThat(response.products().stream()
                                .map(productAssessment -> productAssessment.productId())
                                .toList())
                                .containsExactly("PRODUCT-2", "PRODUCT-1");

                assertThat(response.products()).allSatisfy(productAssessment -> {
                        assertThat(productAssessment.eligible()).isTrue();
                        assertThat(productAssessment.available()).isTrue();
                });

                assertThat(response.products().get(0).funders().stream()
                                .map(funderAssessment -> funderAssessment.funderId())
                                .toList())
                                .containsExactly("FUNDER-2");

                assertThat(response.products().get(1).funders().stream()
                                .map(funderAssessment -> funderAssessment.funderId())
                                .toList())
                                .containsExactly("FUNDER-1");
        }

        @Test
        void eligibleProductWithAcceptingFunderIsAvailable() {
                // given
                given(productRepository.findAll()).willReturn(List.of(product("PRODUCT-1")));
                given(funderRepository.findAll()).willReturn(List.of(
                                funder("FUNDER-1", policy("PRODUCT-1", "0.75", false))));

                // when
                MortgageAssessmentResponse response = service.assess(request("CASE-001"));

                // then
                MortgageAssessmentResponse.ProductAssessment assessment = response.products().get(0);

                assertThat(assessment.eligible()).isTrue();
                assertThat(assessment.available()).isTrue();
                assertThat(assessment.reasons()).isEmpty();
                assertThat(assessment.funders()).containsExactly(
                                new MortgageAssessmentResponse.FunderAssessment(
                                                "FUNDER-1",
                                                "Funder FUNDER-1",
                                                true,
                                                List.of()));
        }

        @Test
        void rejectingFunderMakesEligibleProductUnavailableWithFunderReasons() {
                // given
                given(productRepository.findAll()).willReturn(List.of(product("PRODUCT-1")));
                given(funderRepository.findAll()).willReturn(List.of(
                                funder("FUNDER-1", policy("PRODUCT-1", "0.65", false))));

                // when
                MortgageAssessmentResponse response = service.assess(request("CASE-001"));

                // then
                MortgageAssessmentResponse.ProductAssessment assessment = response.products().get(0);

                assertThat(assessment.eligible()).isTrue();
                assertThat(assessment.available()).isFalse();
                assertThat(assessment.reasons()).isEmpty();
                assertThat(assessment.funders()).containsExactly(
                                new MortgageAssessmentResponse.FunderAssessment(
                                                "FUNDER-1",
                                                "Funder FUNDER-1",
                                                false,
                                                List.of("LTV exceeds the funder's maximum")));
        }

        @Test
        void eligibleProductWithoutMatchingPolicyExplainsWhyItIsUnavailable() {
                // given
                given(productRepository.findAll()).willReturn(List.of(product("PRODUCT-1")));
                given(funderRepository.findAll()).willReturn(List.of(
                                funder("FUNDER-1", policy("OTHER-PRODUCT", "0.75", false))));

                // when
                MortgageAssessmentResponse response = service.assess(request("CASE-001"));

                // then
                MortgageAssessmentResponse.ProductAssessment assessment = response.products().get(0);

                assertThat(assessment.eligible()).isTrue();
                assertThat(assessment.available()).isFalse();
                assertThat(assessment.reasons())
                                .containsExactly("No funder has a funding policy for this product");
                assertThat(assessment.funders()).isEmpty();
        }

        @Test
        void ineligibleProductKeepsProductReasonsAndDoesNotProduceFunderResults() {
                // given
                MortgageProduct hmoProduct = new MortgageProduct(
                                "PRODUCT-1",
                                "Product PRODUCT-1",
                                bd("0.05"),
                                60,
                                new ProductCriteria(
                                                bd("100000"),
                                                bd("1000000"),
                                                bd("0.75"),
                                                bd("0.05"),
                                                bd("1.25"),
                                                Set.of(PropertyType.HMO),
                                                Set.of(ApplicantType.INDIVIDUAL),
                                                Set.of(PropertyLocation.ENGLAND),
                                                false));

                given(productRepository.findAll()).willReturn(List.of(hmoProduct));
                given(funderRepository.findAll()).willReturn(List.of(
                                funder("FUNDER-1", policy("PRODUCT-1", "0.75", false))));

                // when
                MortgageAssessmentResponse response = service.assess(request("CASE-001"));

                // then
                MortgageAssessmentResponse.ProductAssessment assessment = response.products().get(0);

                assertThat(assessment.eligible()).isFalse();
                assertThat(assessment.available()).isFalse();
                assertThat(assessment.reasons())
                                .containsExactly("Property type is not supported by this product");
                assertThat(assessment.funders()).isEmpty();
        }

        @Test
        void onlyPoliciesForTheAssessedProductAppearInFunderResults() {
                // given
                Funder funder = funder(
                                "FUNDER-1",
                                policy("OTHER-PRODUCT", "0.01", true),
                                policy("PRODUCT-1", "0.75", false));

                given(productRepository.findAll()).willReturn(List.of(product("PRODUCT-1")));
                given(funderRepository.findAll()).willReturn(List.of(funder));

                // when
                MortgageAssessmentResponse response = service.assess(request("CASE-001"));

                // then
                MortgageAssessmentResponse.ProductAssessment assessment = response.products().get(0);

                assertThat(assessment.funders()).hasSize(1);
                assertThat(assessment.funders().get(0).funderId()).isEqualTo("FUNDER-1");
                assertThat(assessment.funders().get(0).available()).isTrue();
                assertThat(assessment.funders().get(0).reasons()).isEmpty();
        }

        private static MortgageAssessmentRequest request(String caseReference) {
                return new MortgageAssessmentRequest(
                                caseReference,
                                bd("300000"),
                                new PropertyDetails(
                                                bd("400000"),
                                                bd("10000"),
                                                PropertyType.STANDARD,
                                                PropertyLocation.ENGLAND),
                                new ApplicantDetails(ApplicantType.INDIVIDUAL, false));
        }

        private static MortgageProduct product(String id) {
                return new MortgageProduct(
                                id,
                                "Product " + id,
                                bd("0.05"),
                                60,
                                eligibleCriteria());
        }

        private static ProductCriteria eligibleCriteria() {
                return new ProductCriteria(
                                bd("100000"),
                                bd("1000000"),
                                bd("0.75"),
                                bd("0.05"),
                                bd("1.25"),
                                Set.of(PropertyType.STANDARD),
                                Set.of(ApplicantType.INDIVIDUAL),
                                Set.of(PropertyLocation.ENGLAND),
                                false);
        }

        private static Funder funder(String id, FundingPolicy... policies) {
                return new Funder(id, "Funder " + id, List.of(policies));
        }

        private static FundingPolicy policy(
                        String productId,
                        String maxLtv,
                        boolean landlordExperienceRequired) {
                return new FundingPolicy(
                                productId,
                                Set.of(PropertyLocation.ENGLAND),
                                bd(maxLtv),
                                landlordExperienceRequired);
        }

        private static BigDecimal bd(String value) {
                return new BigDecimal(value);
        }
}