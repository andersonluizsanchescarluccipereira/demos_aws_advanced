package com.comunidade.app.application.ports.out;

import com.comunidade.app.application.core.domain.Veiculo;

import java.io.IOException;

public interface VeiculoRepositoryPort {
    void salvar(Veiculo veiculo) throws IOException;
    Veiculo buscarPorId(String id) throws IOException;
}
