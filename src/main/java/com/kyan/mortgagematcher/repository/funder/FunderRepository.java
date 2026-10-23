package com.kyan.mortgagematcher.repository.funder;

import com.kyan.mortgagematcher.model.funder.Funder;

import java.util.List;
import java.util.Optional;

public interface FunderRepository {
    public List<Funder> findAll();

    public Optional<Funder> findById(final String id);
}
