package com.comunidade.app.adapters.in.controller;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.core.domain.BulkVeiculoRequest;
import com.comunidade.app.application.core.domain.BulkVeiculoResponse;
import com.comunidade.app.application.ports.in.VeiculoServicePort;
import com.comunidade.app.application.ports.in.BulkVeiculoServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoServicePort service;
    private final BulkVeiculoServicePort bulkService;

    public VeiculoController(VeiculoServicePort service, BulkVeiculoServicePort bulkService) {
        this.service = service;
        this.bulkService = bulkService;
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
}
