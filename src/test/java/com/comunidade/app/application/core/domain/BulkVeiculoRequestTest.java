package com.comunidade.app.application.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BulkVeiculoRequestTest {

    private BulkVeiculoRequest request;

    @BeforeEach
    void setUp() {
        request = new BulkVeiculoRequest();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(request);
        assertNull(request.getVeiculos());
        assertEquals(0, request.getSize());
    }

    @Test
    void testParameterizedConstructor() {
        List<Veiculo> veiculos = new ArrayList<>();
        veiculos.add(new Veiculo("1", "Civic", "Honda", 2022));
        veiculos.add(new Veiculo("2", "Corolla", "Toyota", 2021));

        BulkVeiculoRequest req = new BulkVeiculoRequest(veiculos);

        assertEquals(veiculos, req.getVeiculos());
        assertEquals(2, req.getSize());
    }

    @Test
    void testSetVeiculos() {
        List<Veiculo> veiculos = new ArrayList<>();
        veiculos.add(new Veiculo("1", "Civic", "Honda", 2022));

        request.setVeiculos(veiculos);

        assertEquals(veiculos, request.getVeiculos());
        assertEquals(1, request.getSize());
    }

    @Test
    void testGetSizeWithNullVeiculos() {
        request.setVeiculos(null);
        assertEquals(0, request.getSize());
    }

    @Test
    void testGetSizeWithEmptyList() {
        request.setVeiculos(new ArrayList<>());
        assertEquals(0, request.getSize());
    }
}

