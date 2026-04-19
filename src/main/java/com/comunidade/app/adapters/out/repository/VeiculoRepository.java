package com.comunidade.app.adapters.out.repository;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.core.domain.SearchVeiculoResponse;
import com.comunidade.app.application.ports.out.VeiculoRepositoryPort;
import com.comunidade.app.application.ports.out.VeiculoSearchRepositoryPort;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VeiculoRepository implements VeiculoRepositoryPort, VeiculoSearchRepositoryPort {

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

    @Override
    public SearchVeiculoResponse buscarComFiltros(String marca, Integer ano, String modelo, int from, int size) throws IOException {

        boolean hasFilter =
                (marca != null && !marca.isBlank()) ||
                        (ano != null) ||
                        (modelo != null && !modelo.isBlank());

        SearchResponse<Veiculo> response = client.search(s -> s
                        .index(INDEX)
                        .from(from)
                        .size(size)
                        .sort(srt -> srt.field(f -> f.field("ano").order(SortOrder.Desc)))
                        .query(q -> {
                            if (!hasFilter) {
                                return q.matchAll(m -> m);
                            }

                            return q.bool(b -> {
                                if (marca != null && !marca.isBlank()) {
                                    b.must(mq -> mq.term(t -> t.field("marca").value(FieldValue.of(marca))));
                                }

                                if (ano != null) {
                                    b.must(mq -> mq.term(t -> t.field("ano").value(FieldValue.of(ano))));
                                }

                                if (modelo != null && !modelo.isBlank()) {
                                    b.must(mq -> mq.match(m -> m.field("modelo").query(FieldValue.of(modelo))));
                                }

                                return b;
                            });
                        }),
                Veiculo.class
        );

        List<Veiculo> veiculos = response.hits().hits()
                .stream()
                .map(hit -> hit.source())
                .toList();

        long total = response.hits().total() != null
                ? response.hits().total().value()
                : 0;

        int page = size > 0 ? (from / size) + 1 : 1;

        return new SearchVeiculoResponse(veiculos, total, page, size);
    }
}
