package com.kyan.mortgagematcher.repository.funder;

import com.kyan.mortgagematcher.model.funder.Funder;

import java.util.List;

public interface FunderRepository {
    public List<Funder> findAll();

    public List<Funder> findById(final String id);
}
