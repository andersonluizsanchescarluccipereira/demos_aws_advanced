package com.comunidade.app.adapters.in.controller;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.core.domain.BulkVeiculoRequest;
import com.comunidade.app.application.core.domain.BulkVeiculoResponse;
import com.comunidade.app.application.core.domain.SearchVeiculoRequest;
import com.comunidade.app.application.core.domain.SearchVeiculoResponse;
import com.comunidade.app.application.ports.in.VeiculoServicePort;
import com.comunidade.app.application.ports.in.BulkVeiculoServicePort;
import com.comunidade.app.application.ports.in.VeiculoSearchPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoServicePort service;
    private final BulkVeiculoServicePort bulkService;
    private final VeiculoSearchPort searchService;

    public VeiculoController(VeiculoServicePort service, BulkVeiculoServicePort bulkService, VeiculoSearchPort searchService) {
        this.service = service;
        this.bulkService = bulkService;
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Veiculo v) throws Exception {
        service.cadastrar(v);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veiculo> buscar(@PathVariable String id) throws Exception {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping("/bulk")
    public ResponseEntity<BulkVeiculoResponse> criarBulk(@RequestBody BulkVeiculoRequest request) {
        BulkVeiculoResponse response = bulkService.procesarBulk(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchVeiculoResponse> buscarComFiltros(
            @RequestParam(value = "marca", required = false) String marca,
            @RequestParam(value = "ano", required = false) Integer ano,
            @RequestParam(value = "modelo", required = false) String modelo,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) throws IOException {

        if (pageSize > 100) {
            pageSize = 100; // Limitar tamanho máximo para performance
        }

        SearchVeiculoRequest request = new SearchVeiculoRequest(marca, ano, modelo, page, pageSize);
        SearchVeiculoResponse response = searchService.buscarComFiltros(request);

        return ResponseEntity.ok(response);
    }
}
