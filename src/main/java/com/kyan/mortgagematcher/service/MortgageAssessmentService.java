package com.kyan.mortgagematcher.service;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import org.springframework.stereotype.Service;

@Service
public class MortgageAssessmentService {

    public String assess(MortgageAssessmentRequest request) {
        return "Assessment received for case " + request.caseReference();
    }
}