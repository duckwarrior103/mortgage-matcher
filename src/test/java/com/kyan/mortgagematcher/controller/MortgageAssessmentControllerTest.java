package com.kyan.mortgagematcher.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MortgageAssessmentController.class)
class MortgageAssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private final String ASSESSMENT_URI = "/api/v1/mortgage-assessment";

    @Test
    void missingRequiredFieldReturnsBadRequest() throws Exception {
        mockMvc.perform(post(ASSESSMENT_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "caseReference": "",
                          "loanAmount": 100000,
                          "propertyDetails": null,
                          "applicantDetails": null
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors").value(org.hamcrest.Matchers.containsInAnyOrder(
                        "caseReference: must not be blank",
                        "propertyDetails: must not be null",
                        "applicantDetails: must not be null")));
    }

    @Test
    void validRequestReturnsSuccess() throws Exception {
        mockMvc.perform(post(ASSESSMENT_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "caseReference": "CASE-001",
                          "loanAmount": 225000,
                          "propertyDetails": {
                            "value": 300000,
                            "expectedMonthlyRentalIncome": 1500,
                            "type": "STANDARD",
                            "location": "ENGLAND"
                          },
                          "applicantDetails": {
                            "type": "INDIVIDUAL",
                            "isExperienced": true
                          }
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post(ASSESSMENT_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "caseReference": "CASE-001",
                          "loanAmount": 100000
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request body is missing or invalid."))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0]")
                        .value("Check that the body is valid JSON and that values match the expected types."));
    }
}