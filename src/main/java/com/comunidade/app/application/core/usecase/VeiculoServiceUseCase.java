package com.comunidade.app.application.core.usecase;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.ports.out.VeiculoRepositoryPort;
import com.comunidade.app.application.ports.in.VeiculoServicePort;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VeiculoServiceUseCase implements VeiculoServicePort {

    private final VeiculoRepositoryPort repository;

    public VeiculoServiceUseCase(VeiculoRepositoryPort repository) {
        this.repository = repository;
    }

    public void cadastrar(Veiculo v) throws IOException {
        repository.salvar(v);
    }

    public Veiculo buscar(String id) throws IOException {
        return repository.buscarPorId(id);
    }
}
