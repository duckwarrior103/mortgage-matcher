package com.kyan.mortgagematcher.service;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.model.funder.Funder;
import com.kyan.mortgagematcher.model.product.MortgageProduct;
import com.kyan.mortgagematcher.repository.funder.FunderRepository;
import com.kyan.mortgagematcher.repository.product.ProductRepository;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MortgageAssessmentService {

    private final ProductRepository productRepository;
    private final FunderRepository funderRepository;

    public MortgageAssessmentService(ProductRepository productRepository, FunderRepository funderRepository) {
        this.productRepository = productRepository;
        this.funderRepository = funderRepository;
    }

    public String assess(MortgageAssessmentRequest request) {
        // TODO: Process Request / create assessment model for data persistence?

        // TODO: Retrieve stash of products, funders and funding policies
        List<MortgageProduct> mortgageProducts = productRepository.findAll();
        List<Funder> funders = funderRepository.findAll();

        // TODO: call MATCHING LOGIC

        // TODO: Curate the assessment response AND/OR persist it (in the future?)

        return "Assessment received for case " + request.caseReference();
    }
}