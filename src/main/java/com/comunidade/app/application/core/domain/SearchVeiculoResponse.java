package com.comunidade.app.application.core.domain;

import java.util.List;

public class SearchVeiculoResponse {
    private List<Veiculo> veiculos;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    public SearchVeiculoResponse() {}

    public SearchVeiculoResponse(List<Veiculo> veiculos, long total, int page, int pageSize) {
        this.veiculos = veiculos;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public List<Veiculo> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<Veiculo> veiculos) {
        this.veiculos = veiculos;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
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

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

