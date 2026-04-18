package com.testes.demos_aws.service;

import com.testes.demos_aws.model.Veiculo;

public interface VeiculoService {
    void cadastrar(Veiculo v) throws Exception;
    Veiculo buscar(String id) throws Exception;
}
