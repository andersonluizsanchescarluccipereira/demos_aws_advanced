package com.comunidade.app.application.ports.out;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.core.domain.SearchVeiculoResponse;

import java.io.IOException;

public interface VeiculoSearchRepositoryPort {
    SearchVeiculoResponse buscarComFiltros(String marca, Integer ano, String modelo, int from, int size) throws IOException;
}

