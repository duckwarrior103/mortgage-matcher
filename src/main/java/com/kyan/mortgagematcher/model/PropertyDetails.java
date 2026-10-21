package com.kyan.mortgagematcher.model;

import java.math.BigDecimal;

public record PropertyDetails(
        BigDecimal value,
        BigDecimal expectedMonthlyRentalIncome,
        PropertyType type,
        PropertyLocation location

) {
}