package com.comunidade.app.application.core.domain;

import java.util.List;

public class SearchVeiculoRequest {
    private String marca;
    private Integer ano;
    private String modelo;
    private int page;
    private int pageSize;

    public SearchVeiculoRequest() {}

    public SearchVeiculoRequest(String marca, Integer ano, String modelo, int page, int pageSize) {
        this.marca = marca;
        this.ano = ano;
        this.modelo = modelo;
        this.page = page;
        this.pageSize = pageSize;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getFrom() {
        return (page - 1) * pageSize;
    }
}

