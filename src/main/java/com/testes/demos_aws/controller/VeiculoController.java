package com.testes.demos_aws.controller;

import com.testes.demos_aws.model.Veiculo;
import com.testes.demos_aws.service.VeiculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
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
