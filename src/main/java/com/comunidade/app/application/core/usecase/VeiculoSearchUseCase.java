package com.comunidade.app.application.core.usecase;

import com.comunidade.app.application.core.domain.SearchVeiculoRequest;
import com.comunidade.app.application.core.domain.SearchVeiculoResponse;
import com.comunidade.app.application.ports.in.VeiculoSearchPort;
import com.comunidade.app.application.ports.out.VeiculoSearchRepositoryPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VeiculoSearchUseCase implements VeiculoSearchPort {

    private static final Logger logger = LoggerFactory.getLogger(VeiculoSearchUseCase.class);

    private final VeiculoSearchRepositoryPort repository;

    public VeiculoSearchUseCase(VeiculoSearchRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    @CircuitBreaker(name = "buscarVeiculos", fallbackMethod = "fallbackBuscarComFiltros")
    @Retry(name = "buscarVeiculos")
    public SearchVeiculoResponse buscarComFiltros(SearchVeiculoRequest request) throws IOException {
        logger.info("Buscando veículos com filtros: marca={}, ano={}, modelo={}, page={}, pageSize={}",
                request.getMarca(), request.getAno(), request.getModelo(), request.getPage(), request.getPageSize());

        SearchVeiculoResponse response = repository.buscarComFiltros(
                request.getMarca(),
                request.getAno(),
                request.getModelo(),
                request.getFrom(),
                request.getPageSize()
        );

        logger.info("Busca realizada com sucesso: total={}, page={}, totalPages={}",
                response.getTotal(), request.getPage(), response.getTotalPages());

        return response;
    }

    public SearchVeiculoResponse fallbackBuscarComFiltros(SearchVeiculoRequest request, Exception e) {
        logger.error("Fallback: Erro ao buscar veículos", e);
        return new SearchVeiculoResponse();
    }
}

