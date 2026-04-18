package com.comunidade.app.application.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoTest {

    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        veiculo = new Veiculo();
    }

    @Test
    void testVeiculoDefaultConstructor() {
        assertNotNull(veiculo);
        assertNull(veiculo.getId());
        assertNull(veiculo.getModelo());
        assertNull(veiculo.getMarca());
        assertEquals(0, veiculo.getAno());
    }

    @Test
    void testVeiculoParameterizedConstructor() {
        Veiculo v = new Veiculo("1", "Civic", "Honda", 2022);
        assertEquals("1", v.getId());
        assertEquals("Civic", v.getModelo());
        assertEquals("Honda", v.getMarca());
        assertEquals(2022, v.getAno());
    }

    @Test
    void testSetAndGetId() {
        veiculo.setId("123");
        assertEquals("123", veiculo.getId());
    }

    @Test
    void testSetAndGetModelo() {
        veiculo.setModelo("Corolla");
        assertEquals("Corolla", veiculo.getModelo());
    }

    @Test
    void testSetAndGetMarca() {
        veiculo.setMarca("Toyota");
        assertEquals("Toyota", veiculo.getMarca());
    }

    @Test
    void testSetAndGetAno() {
        veiculo.setAno(2023);
        assertEquals(2023, veiculo.getAno());
    }

    @Test
    void testVeiculoAllFields() {
        veiculo.setId("456");
        veiculo.setModelo("Civic");
        veiculo.setMarca("Honda");
        veiculo.setAno(2022);

        assertEquals("456", veiculo.getId());
        assertEquals("Civic", veiculo.getModelo());
        assertEquals("Honda", veiculo.getMarca());
        assertEquals(2022, veiculo.getAno());
    }

    @Test
    void testVeiculoNullValues() {
        veiculo.setId(null);
        veiculo.setModelo(null);
        veiculo.setMarca(null);

        assertNull(veiculo.getId());
        assertNull(veiculo.getModelo());
        assertNull(veiculo.getMarca());
    }
}

