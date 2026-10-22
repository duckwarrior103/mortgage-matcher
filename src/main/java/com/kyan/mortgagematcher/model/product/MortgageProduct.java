package com.kyan.mortgagematcher.model.product;

import java.math.BigDecimal;

public record MortgageProduct(
        String id,
        String name,
        BigDecimal annualInterestRate,
        int fixedRateMonths,
        ProductCriteria criteria) {
}
