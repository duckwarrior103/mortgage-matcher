package com.kyan.mortgagematcher.service;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import org.springframework.stereotype.Service;

@Service
public class MortgageAssessmentService {

    public String assess(MortgageAssessmentRequest request) {
        // TODO: Process Request / create assessment model for data persistence?

        // TODO: Retrieve stash of products, funders and funding policies

        // TODO: call MATCHING LOGIC

        // TODO: Curate the assessment response AND/OR persist it (in the future?)

        return "Assessment received for case " + request.caseReference();
    }
}