package com.kyan.mortgagematcher.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;
import com.kyan.mortgagematcher.api.dto.MortgageAssessmentResponse;
import com.kyan.mortgagematcher.service.assessment.MortgageAssessmentService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
public class MortgageAssessmentController {
    private final MortgageAssessmentService mortgageAssessmentService;

    public MortgageAssessmentController(
            MortgageAssessmentService mortgageAssessmentService) {
        this.mortgageAssessmentService = mortgageAssessmentService;
    }

    @PostMapping("/mortgage-assessment")
    public MortgageAssessmentResponse assess(@Valid @RequestBody MortgageAssessmentRequest mortgageAssessmentRequest) {
        return mortgageAssessmentService.assess(mortgageAssessmentRequest);
    }

}