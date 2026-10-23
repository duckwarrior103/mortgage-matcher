package com.kyan.mortgagematcher.model.product;

import com.kyan.mortgagematcher.model.applicant.ApplicantType;
import com.kyan.mortgagematcher.model.property.PropertyType;
import com.kyan.mortgagematcher.model.property.PropertyLocation;

import java.math.BigDecimal;
import java.util.Set;

public record ProductCriteria(
        BigDecimal minLoanAmount,
        BigDecimal maxLoanAmount,
        BigDecimal maxLtv,
        BigDecimal annualStressRate,
        BigDecimal minIcr,
        Set<PropertyType> allowedPropertyTypes,
        Set<ApplicantType> allowedApplicantTypes,
        Set<PropertyLocation> allowedLocations,
        boolean landlordExperienceRequired) {
}
