package com.kyan.mortgagematcher.repository.product;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import com.kyan.mortgagematcher.model.product.MortgageProduct;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.core.io.Resource;

@Repository
public class JsonProductRepository implements ProductRepository {

    // object initialised with the list of mortgage products
    private final List<MortgageProduct> mortgageProducts;

    // Constructor which loads in Products json from resources
    public JsonProductRepository(ResourceLoader resourceLoader, JsonMapper jsonMapper) {
        Resource resource = resourceLoader.getResource(
                "classpath:catalogue/products.json");

        try (var input = resource.getInputStream()) {
            this.mortgageProducts = List.copyOf(
                    jsonMapper.readValue(
                            input,
                            new TypeReference<List<MortgageProduct>>() {
                            }));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load catalogue/products.json", exception);
        }
    }

    @Override
    public List<MortgageProduct> findAll() {
        return mortgageProducts;
    }

    @Override
    public List<MortgageProduct> findById(final String id) {
        return mortgageProducts
                .stream()
                .filter(mortgageProduct -> mortgageProduct.id().equals(id))
                .toList();
    }
}