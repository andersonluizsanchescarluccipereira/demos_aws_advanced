package com.comunidade.app.application.core.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BulkVeiculoResponseTest {

    private BulkVeiculoResponse response;

    @BeforeEach
    void setUp() {
        response = new BulkVeiculoResponse();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(response);
    }

    @Test
    void testParameterizedConstructor() {
        BulkVeiculoResponse resp = new BulkVeiculoResponse(100, 95, 5, "Sucesso", "COMPLETED");

        assertEquals(100, resp.getTotalProcessado());
        assertEquals(95, resp.getSucessos());
        assertEquals(5, resp.getErros());
        assertEquals("Sucesso", resp.getMensagem());
        assertEquals("COMPLETED", resp.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        response.setTotalProcessado(100);
        response.setSucessos(95);
        response.setErros(5);
        response.setMensagem("Processamento concluído");
        response.setStatus("COMPLETED");

        assertEquals(100, response.getTotalProcessado());
        assertEquals(95, response.getSucessos());
        assertEquals(5, response.getErros());
        assertEquals("Processamento concluído", response.getMensagem());
        assertEquals("COMPLETED", response.getStatus());
    }

    @Test
    void testZeroValues() {
        response.setTotalProcessado(0);
        response.setSucessos(0);
        response.setErros(0);

        assertEquals(0, response.getTotalProcessado());
        assertEquals(0, response.getSucessos());
        assertEquals(0, response.getErros());
    }

    @Test
    void testStatusValues() {
        response.setStatus("PROCESSING");
        assertEquals("PROCESSING", response.getStatus());

        response.setStatus("ERROR");
        assertEquals("ERROR", response.getStatus());

        response.setStatus("FALLBACK");
        assertEquals("FALLBACK", response.getStatus());
    }
}

