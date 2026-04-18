package com.comunidade.app.application.core.usecase;

import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.ports.out.VeiculoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceUseCaseTest {

    @Mock
    private VeiculoRepositoryPort repository;

    private VeiculoServiceUseCase service;

    @BeforeEach
    void setUp() {
        service = new VeiculoServiceUseCase(repository);
    }

    @Test
    void testCadastrarVeiculo() throws IOException {
        Veiculo veiculo = new Veiculo("1", "Civic", "Honda", 2022);
        doNothing().when(repository).salvar(any(Veiculo.class));

        service.cadastrar(veiculo);

        verify(repository, times(1)).salvar(veiculo);
    }

    @Test
    void testCadastrarVeiculoComDadosCompletos() throws IOException {
        Veiculo veiculo = new Veiculo("123", "Corolla", "Toyota", 2021);
        doNothing().when(repository).salvar(any(Veiculo.class));

        service.cadastrar(veiculo);

        verify(repository, times(1)).salvar(veiculo);
    }

    @Test
    void testBuscarVeiculo() throws IOException {
        Veiculo veiculoEsperado = new Veiculo("1", "Civic", "Honda", 2022);
        when(repository.buscarPorId("1")).thenReturn(veiculoEsperado);

        Veiculo resultado = service.buscar("1");

        assertEquals(veiculoEsperado, resultado);
        verify(repository, times(1)).buscarPorId("1");
    }

    @Test
    void testBuscarVeiculoNaoEncontrado() throws IOException {
        when(repository.buscarPorId("999")).thenReturn(null);

        Veiculo resultado = service.buscar("999");

        assertNull(resultado);
        verify(repository, times(1)).buscarPorId("999");
    }

    @Test
    void testCadastrarVeiculoThrowsIOException() throws IOException {
        Veiculo veiculo = new Veiculo("1", "Civic", "Honda", 2022);
        doThrow(new IOException("Database error")).when(repository).salvar(any(Veiculo.class));

        assertThrows(IOException.class, () -> service.cadastrar(veiculo));
        verify(repository, times(1)).salvar(veiculo);
    }

    @Test
    void testBuscarVeiculoThrowsIOException() throws IOException {
        when(repository.buscarPorId("1")).thenThrow(new IOException("Database error"));

        assertThrows(IOException.class, () -> service.buscar("1"));
        verify(repository, times(1)).buscarPorId("1");
    }

    @Test
    void testCadastrarMultiplosVeiculos() throws IOException {
        Veiculo v1 = new Veiculo("1", "Civic", "Honda", 2022);
        Veiculo v2 = new Veiculo("2", "Corolla", "Toyota", 2021);
        doNothing().when(repository).salvar(any(Veiculo.class));

        service.cadastrar(v1);
        service.cadastrar(v2);

        verify(repository, times(2)).salvar(any(Veiculo.class));
    }
}

