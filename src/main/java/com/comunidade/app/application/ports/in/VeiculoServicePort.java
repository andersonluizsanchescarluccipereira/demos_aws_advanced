package com.comunidade.app.application.ports.in;

import com.comunidade.app.application.core.domain.Veiculo;

public interface VeiculoServicePort {
    void cadastrar(Veiculo v) throws Exception;
    Veiculo buscar(String id) throws Exception;
}
