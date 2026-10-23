package com.kyan.mortgagematcher.repository.funder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import com.kyan.mortgagematcher.model.funder.Funder;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFunderRepository implements FunderRepository {

    // object initialised with list of funders
    private final List<Funder> funders;

    public JsonFunderRepository(ResourceLoader resourceLoader, JsonMapper jsonMapper) {
        // Load the funder json resource from catalogue
        Resource resource = resourceLoader.getResource(
                "classpath:catalogue/funders.json");

        try (var input = resource.getInputStream()) {
            this.funders = List.copyOf(
                    jsonMapper.readValue(input, new TypeReference<List<Funder>>() {

                    }));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load catalogue/funders.json", exception);
        }

    }

    @Override
    public List<Funder> findAll() {
        return funders;
    }

    @Override
    public Optional<Funder> findById(String id) {
        return funders.stream()
                .filter(funder -> funder.id().equals(id))
                .findFirst();
    }
}
