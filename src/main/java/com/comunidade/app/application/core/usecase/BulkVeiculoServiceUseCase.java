package com.comunidade.app.application.core.usecase;

import com.comunidade.app.application.core.domain.BulkVeiculoRequest;
import com.comunidade.app.application.core.domain.BulkVeiculoResponse;
import com.comunidade.app.application.core.domain.Veiculo;
import com.comunidade.app.application.ports.in.BulkVeiculoServicePort;
import com.comunidade.app.application.ports.out.DistributedLockPort;
import com.comunidade.app.application.ports.out.VeiculoRepositoryPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BulkVeiculoServiceUseCase implements BulkVeiculoServicePort {

    private static final Logger logger = LoggerFactory.getLogger(BulkVeiculoServiceUseCase.class);
    private static final int BATCH_SIZE = 500;
    private static final String BULK_LOCK_PREFIX = "bulk_veiculo_";
    private static final long LOCK_TIMEOUT = 300; // 5 minutos

    private static class BatchResult {
        int sucessos;
        int erros;
        String status;

        BatchResult(int sucessos, int erros, String status) {
            this.sucessos = sucessos;
            this.erros = erros;
            this.status = status;
        }
    }

    private final VeiculoRepositoryPort repository;
    private final DistributedLockPort distributedLock;
    private final ExecutorService executorService;

    public BulkVeiculoServiceUseCase(VeiculoRepositoryPort repository, DistributedLockPort distributedLock) {
        this.repository = repository;
        this.distributedLock = distributedLock;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    @Override
    @CircuitBreaker(name = "bulkVeiculo", fallbackMethod = "fallbackProcessarBulk")
    @Retry(name = "bulkVeiculo")
    public BulkVeiculoResponse procesarBulk(BulkVeiculoRequest request) {
        String lockId = UUID.randomUUID().toString();
        String lockKey = BULK_LOCK_PREFIX + (request.getRequestId() != null ? request.getRequestId() : String.valueOf(request.hashCode()));

        logger.info("Iniciando processamento bulk com {} veículos", request.getSize());

        // Adquirir lock distribuído para evitar duplicação em múltiplos pods
        boolean acquired = distributedLock.acquireLock(lockKey, lockId, LOCK_TIMEOUT);
        if (!acquired) {
            // Verificar se já foi processado
            Optional<String> currentLock = distributedLock.getLock(lockKey);
            if (currentLock.isPresent() && currentLock.get().startsWith("PROCESSED")) {
                String[] parts = currentLock.get().split("_");
                long processedAt = Long.parseLong(parts[1]);
                String message = "Bulk já foi processado anteriormente em " + new Date(processedAt);
                logger.info(message);
                return new BulkVeiculoResponse(
                        0, 0, 0,
                        message,
                        "ALREADY_PROCESSED",
                        new ArrayList<>()
                );
            } else {
                logger.warn("Não foi possível adquirir lock distribuído. Outro pod já está processando.");
                return new BulkVeiculoResponse(
                        0, 0, 0,
                        "Processamento já em andamento em outro pod",
                        "PROCESSING",
                        new ArrayList<>()
                );
            }
        }

        try {
            BulkVeiculoResponse response = processarBulkInterno(request, lockKey, lockId);
            // Marcar como processado
            distributedLock.releaseLock(lockKey, lockId);
            String processedValue = "PROCESSED_" + System.currentTimeMillis() + "_" + (request.getRequestId() != null ? request.getRequestId() : String.valueOf(request.hashCode()));
            distributedLock.acquireLock(lockKey, processedValue, 86400L); // 1 dia
            return response;
        } catch (Exception e) {
            distributedLock.releaseLock(lockKey, lockId);
            throw e;
        }
    }

    private BulkVeiculoResponse processarBulkInterno(BulkVeiculoRequest request, String lockKey, String lockId) {
        List<Veiculo> veiculos = request.getVeiculos();
        int totalProcessado = 0;
        int sucessos = 0;
        int erros = 0;
        List<String> batchStatuses = new ArrayList<>();

        try {
            List<CompletableFuture<BatchResult>> futures = new ArrayList<>();
            int totalBatches = (veiculos.size() + BATCH_SIZE - 1) / BATCH_SIZE;

            // Processar em batches de 500
            for (int i = 0; i < veiculos.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, veiculos.size());
                List<Veiculo> batch = new ArrayList<>(veiculos.subList(i, end));
                int batchNumber = (i / BATCH_SIZE) + 1;

                logger.info("Iniciando processamento do batch {} de {} com {} veículos", batchNumber, totalBatches, batch.size());

                CompletableFuture<BatchResult> future = CompletableFuture.supplyAsync(
                        () -> processarBatch(batch, batchNumber),
                        executorService
                );
                futures.add(future);
            }

            // Aguardar conclusão de todos os batches
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            allFutures.join();

            // Somar resultados
            for (CompletableFuture<BatchResult> future : futures) {
                try {
                    BatchResult result = future.get();
                    sucessos += result.sucessos;
                    erros += result.erros;
                    batchStatuses.add(result.status);
                } catch (Exception e) {
                    logger.error("Erro ao processar batch", e);
                    erros++;
                    batchStatuses.add("Erro no batch: " + e.getMessage());
                }
            }

            totalProcessado = sucessos + erros;

            logger.info("Bulk processado: Total={}, Sucessos={}, Erros={}", totalProcessado, sucessos, erros);

            return new BulkVeiculoResponse(
                    totalProcessado,
                    sucessos,
                    erros,
                    "Processamento concluído com sucesso",
                    "COMPLETED",
                    batchStatuses
            );

        } catch (Exception e) {
            logger.error("Erro ao processar bulk", e);
            return new BulkVeiculoResponse(
                    totalProcessado,
                    sucessos,
                    erros,
                    "Erro ao processar bulk: " + e.getMessage(),
                    "ERROR",
                    batchStatuses
            );
        }
    }

     private BatchResult processarBatch(List<Veiculo> batch, int batchNumber) {
         int sucessos = 0;
         int erros = 0;
         for (Veiculo veiculo : batch) {
             try {
                 processarComRetry(veiculo);
                 sucessos++;
             } catch (Exception e) {
                 logger.warn("Erro ao processar veículo {}: {}", veiculo.getId(), e.getMessage(), e);
                 erros++;
             }
         }
        int errosBatch = batch.size() - sucessos;
        String status = "Batch " + batchNumber + ": " + sucessos + " sucessos, " + errosBatch + " erros";
        logger.info(status);
        return new BatchResult(sucessos, errosBatch, status);
    }

    @Retry(name = "salvarVeiculo")
    @CircuitBreaker(name = "salvarVeiculo", fallbackMethod = "fallbackSalvarVeiculo")
    private void processarComRetry(Veiculo veiculo) throws IOException {
        repository.salvar(veiculo);
    }

    public BulkVeiculoResponse fallbackProcessarBulk(BulkVeiculoRequest request, Exception e) {
        logger.error("Fallback: Erro ao processar bulk", e);
        return new BulkVeiculoResponse(
                0, 0, 0,
                "Falha no processamento: Circuit breaker aberto. " + e.getMessage(),
                "FALLBACK",
                new ArrayList<>()
        );
    }

    public void fallbackSalvarVeiculo(Veiculo veiculo, Exception e) throws IOException {
        logger.error("Fallback: Erro ao salvar veículo {}. Tentando cache...", veiculo.getId(), e);
        // Implementar fallback para cache se necessário
        throw new IOException("Falha ao salvar após retries");
    }
}
