package com.kyan.mortgagematcher.repository.product;

import com.kyan.mortgagematcher.model.product.MortgageProduct;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class JsonProductRepositoryTest {

    private final ProductRepository repository = new JsonProductRepository(
            new DefaultResourceLoader(),
            JsonMapper.builder().build());

    @Test
    void loadsProductsFromClasspathJson() {
        assertThat(repository.findAll()).isNotEmpty();
    }

    @Test
    void findsProductById() {
        MortgageProduct product = repository.findById("STANDARD_65").orElseThrow();

        assertThat(product.name()).isEqualTo("Standard Buy-to-Let 65");
    }

    @Test
    void returnsEmptyWhenProductIdDoesNotExist() {
        assertThat(repository.findById("DOES_NOT_EXIST")).isEmpty();
    }
}