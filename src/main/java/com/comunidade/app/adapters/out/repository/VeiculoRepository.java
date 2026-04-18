package com.comunidade.app.adapters.out.repository;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.ports.out.VeiculoRepositoryPort;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetResponse;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class VeiculoRepository implements VeiculoRepositoryPort {

    private final OpenSearchClient client;
    private final String INDEX = "veiculos";

    public VeiculoRepository(OpenSearchClient client) {
        this.client = client;
    }

    public void salvar(Veiculo veiculo) throws IOException {
        client.index(i -> i
                .index(INDEX)
                .id(veiculo.getId())
                .document(veiculo)
        );
    }

    public Veiculo buscarPorId(String id) throws IOException {
        GetResponse<Veiculo> response = client.get(g -> g
                        .index(INDEX)
                        .id(id),
                Veiculo.class
        );

        return response.source();
    }
}
