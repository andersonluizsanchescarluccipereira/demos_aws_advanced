package com.comunidade.app.application.ports.in;

import com.comunidade.app.application.core.domain.SearchVeiculoRequest;
import com.comunidade.app.application.core.domain.SearchVeiculoResponse;

import java.io.IOException;

public interface VeiculoSearchPort {
    SearchVeiculoResponse buscarComFiltros(SearchVeiculoRequest request) throws IOException;
}

