package com.kyan.mortgagematcher.model.funder;

import java.util.List;

public record Funder(
        String id,
        String name,
        List<FundingPolicy> policies) {
}
