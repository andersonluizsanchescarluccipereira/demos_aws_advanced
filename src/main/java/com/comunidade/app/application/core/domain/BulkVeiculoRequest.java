package com.comunidade.app.application.core.domain;

import java.util.List;

public class BulkVeiculoRequest {
    private String requestId;
    private List<Veiculo> veiculos;

    public BulkVeiculoRequest() {
    }

    public BulkVeiculoRequest(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public BulkVeiculoRequest(String requestId, List<Veiculo> veiculos) {
        this.requestId = requestId;
        this.veiculos = veiculos;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
