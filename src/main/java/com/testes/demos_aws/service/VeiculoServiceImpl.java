package com.testes.demos_aws.service;

import com.testes.demos_aws.model.Veiculo;
import com.testes.demos_aws.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VeiculoServiceImpl implements VeiculoService {

    private final VeiculoRepository repository;

    public VeiculoServiceImpl(VeiculoRepository repository) {
        this.repository = repository;
    }

    public void cadastrar(Veiculo v) throws IOException {
        repository.salvar(v);
    }

    public Veiculo buscar(String id) throws IOException {
        return repository.buscarPorId(id);
    }
}
