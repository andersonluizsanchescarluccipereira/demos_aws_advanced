package com.testes.demos_aws.repository;

import com.testes.demos_aws.model.Veiculo;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetResponse;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class VeiculoRepository {

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
