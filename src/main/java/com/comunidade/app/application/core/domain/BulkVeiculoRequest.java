package com.comunidade.app.application.core.domain;

import java.util.List;

public class BulkVeiculoRequest {
    private List<Veiculo> veiculos;

    public BulkVeiculoRequest() {
    }

    public BulkVeiculoRequest(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public int getSize() {
        return veiculos != null ? veiculos.size() : 0;
    }
}

