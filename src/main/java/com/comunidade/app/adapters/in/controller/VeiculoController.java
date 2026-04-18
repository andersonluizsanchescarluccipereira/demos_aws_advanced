package com.comunidade.app.adapters.in.controller;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.ports.in.VeiculoServicePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoServicePort service;

    public VeiculoController(VeiculoServicePort service) {
        this.service = service;
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
}
