package com.kyan.mortgagematcher.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PropertyDetails(
                @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal value,

                @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal expectedMonthlyRentalIncome,

                @NotNull PropertyType type,
                @NotNull PropertyLocation location) {
}