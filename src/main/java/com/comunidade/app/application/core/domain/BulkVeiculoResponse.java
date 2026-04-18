package com.comunidade.app.application.core.domain;

public class BulkVeiculoResponse {
    private int totalProcessado;
    private int sucessos;
    private int erros;
    private String mensagem;
    private String status;

    public BulkVeiculoResponse() {
    }

    public BulkVeiculoResponse(int totalProcessado, int sucessos, int erros, String mensagem, String status) {
        this.totalProcessado = totalProcessado;
        this.sucessos = sucessos;
        this.erros = erros;
        this.mensagem = mensagem;
        this.status = status;
    }

    public int getTotalProcessado() {
        return totalProcessado;
    }

    public void setTotalProcessado(int totalProcessado) {
        this.totalProcessado = totalProcessado;
    }

    public int getSucessos() {
        return sucessos;
    }

    public void setSucessos(int sucessos) {
        this.sucessos = sucessos;
    }

    public int getErros() {
        return erros;
    }

    public void setErros(int erros) {
        this.erros = erros;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

