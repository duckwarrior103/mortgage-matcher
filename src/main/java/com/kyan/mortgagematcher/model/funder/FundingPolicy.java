package com.kyan.mortgagematcher.model.funder;

import com.kyan.mortgagematcher.model.property.PropertyLocation;
import java.math.BigDecimal;
import java.util.Set;

public record FundingPolicy(
        String productId,
        Set<PropertyLocation> allowedLocations,
        BigDecimal maxLtv,
        boolean landlordExperienceRequired) {
}