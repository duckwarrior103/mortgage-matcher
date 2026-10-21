package com.kyan.mortgagematcher.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kyan.mortgagematcher.api.dto.MortgageAssessmentRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
public class MortgageAssessmentController {

    @PostMapping("/mortgage-assessment")
    public String assess(@Valid @RequestBody MortgageAssessmentRequest mortgageAssessmentRequest) {
        // TODO: process POST request

        // Use assessmentService to access the request: TODO

        return "This is the mortgage assessment endpoint.";
    }

}