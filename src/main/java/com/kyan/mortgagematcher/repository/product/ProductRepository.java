package com.kyan.mortgagematcher.repository.product;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.kyan.mortgagematcher.model.product.MortgageProduct;

// For now, a product repository only has two methods to be implemented
@Repository
public interface ProductRepository {
    public List<MortgageProduct> findAll();

    public Optional<MortgageProduct> findById(final String id);
}
