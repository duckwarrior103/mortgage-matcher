package com.kyan.mortgagematcher.repository.funder;

import com.kyan.mortgagematcher.model.funder.Funder;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

class JsonFunderRepositoryTest {

    private final FunderRepository repository = new JsonFunderRepository(
            new DefaultResourceLoader(),
            JsonMapper.builder().build());

    @Test
    void loadsFundersFromClasspathJson() {
        List<Funder> funders = repository.findAll();

        assertThat(funders).hasSize(3);
        assertThat(funders.get(0).id()).isEqualTo("NORTHSTAR");
        assertThat(funders.get(1).id()).isEqualTo("HORIZON");
        assertThat(funders.get(2).id()).isEqualTo("SPECIALIST");
    }

    @Test
    void findsFunderById() {
        Funder funder = repository.findById("HORIZON").orElseThrow();

        assertThat(funder.name()).isEqualTo("Horizon Lending Fund");
        assertThat(funder.policies()).hasSize(2);
    }

    @Test
    void returnsEmptyWhenFunderIdDoesNotExist() {
        assertThat(repository.findById("DOES_NOT_EXIST")).isEmpty();
    }
}