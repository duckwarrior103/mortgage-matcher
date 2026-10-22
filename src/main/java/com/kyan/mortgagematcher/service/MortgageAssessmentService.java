package com.kyan.mortgagematcher.service;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.model.product.MortgageProduct;
import com.kyan.mortgagematcher.repository.product.ProductRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MortgageAssessmentService {

    private final ProductRepository productRepository;

    public MortgageAssessmentService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String assess(MortgageAssessmentRequest request) {
        // TODO: Process Request / create assessment model for data persistence?

        // TODO: Retrieve stash of products, funders and funding policies
        List<MortgageProduct> mortgageProducts = productRepository.findAll();

        // TODO: call MATCHING LOGIC

        // TODO: Curate the assessment response AND/OR persist it (in the future?)

        return "Assessment received for case " + request.caseReference();
    }
}