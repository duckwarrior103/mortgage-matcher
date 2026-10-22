package com.kyan.mortgagematcher.repository;

import java.util.List;

import com.kyan.mortgagematcher.model.product.MortgageProduct;

// For now, a product repository only has two methods to be implemented
public interface ProductRepository {
    public List<MortgageProduct> findAll();

    public List<MortgageProduct> findById();
}
