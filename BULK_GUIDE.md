# Guia de Uso - Feature Bulk Import de Veículos

## Overview
Sistema resiliente e escalável para importação em lote de até 60.000 registros de veículos com:
- **Batch Processing**: Processa 500 registros por vez
- **Distributed Lock**: Evita duplicação em múltiplos pods
- **Circuit Breaker**: Protege contra falhas em cascata
- **Retry com Exponential Backoff**: Recuperação automática de falhas temporárias
- **Fallback Pattern**: Comportamento gracioso em caso de degradação

## Endpoint

### POST /veiculos/bulk
Importa múltiplos veículos em lote (suporta até 60.000 registros)

**Request:**
```bash
curl -X POST http://localhost:8080/veiculos/bulk \
  -H "Content-Type: application/json" \
  -d '{
    "veiculos": [
      {
        "id": "1",
        "modelo": "Civic",
        "marca": "Honda",
        "ano": 2022
      },
      {
        "id": "2",
        "modelo": "Corolla",
        "marca": "Toyota",
        "ano": 2021
      }
      // ... até 60.000 registros
    ]
  }'
```

**Response (Sucesso):**
```json
{
  "totalProcessado": 60000,
  "sucessos": 59950,
  "erros": 50,
  "mensagem": "Processamento concluído com sucesso",
  "status": "COMPLETED"
}
```

**Response (Em Processamento em outro pod):**
```json
{
  "totalProcessado": 0,
  "sucessos": 0,
  "erros": 0,
  "mensagem": "Processamento já em andamento em outro pod",
  "status": "PROCESSING"
}
```

**Response (Circuit Breaker Ativo):**
```json
{
  "totalProcessado": 0,
  "sucessos": 0,
  "erros": 0,
  "mensagem": "Falha no processamento: Circuit breaker aberto",
  "status": "FALLBACK"
}
```

## Arquitetura

### Componentes Principais

1. **BulkVeiculoServiceUseCase**
   - Orquestra o processamento em lote
   - Adquire lock distribuído
   - Divide dados em batches de 500
   - Processa batches em paralelo (4 threads)

2. **RedisDistributedLock**
   - Implementa lock distribuído usando Redis
   - Previne duplicação entre múltiplos pods
   - Timeout de 10 minutos

3. **Resilience4j**
   - **Circuit Breaker**: Falha rápido quando taxa de erro > 50%
   - **Retry**: Até 5 tentativas com exponential backoff
   - **Fallback**: Método alternativo em caso de falha

4. **VeiculoRepository**
   - Salva dados no OpenSearch
   - Implementa retry automático

## Características de Resiliência

### 1. Processamento em Paralelo
- 4 threads processam batches de 500 registros simultaneamente
- Melhora significativa de throughput
- Evita travamento da aplicação

### 2. Distributed Lock
```redis
SET bulk_veiculo_<hash> <uuid> EX 600 NX
```
- Garante que apenas 1 pod processa o mesmo bulk
- TTL de 10 minutos
- Liberado automaticamente após conclusão

### 3. Retry com Backoff Exponencial
- Primeira tentativa: Imediata
- Segunda: 300ms
- Terceira: 450ms
- Quarta: 675ms
- Quinta: 1012ms

### 4. Circuit Breaker
Estados:
- **CLOSED**: Operações normais
- **OPEN**: Falhas bloqueadas (50% taxa de erro)
- **HALF_OPEN**: Teste de recuperação

### 5. Tratamento de Erros
- Erros em registros individuais não afetam outros
- Logs detalhados de cada falha
- Retorna estatísticas de sucesso/erro

## Configuração

### application.yml
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 10s
    
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
```

## Monitoramento

### Logs
```
[INFO] Iniciando processamento bulk com 60000 veículos
[INFO] Bulk processado: Total=60000, Sucessos=59950, Erros=50
[WARN] Não foi possível adquirir lock distribuído
[ERROR] Erro ao processar veículo 123: Connection timeout
```

### Métricas
- Total de registros processados
- Taxa de sucesso
- Taxa de erro
- Tempo de processamento
- Estado do Circuit Breaker

## Escalabilidade

### Múltiplos Pods
- Distributed lock previne processamento duplicado
- Cada pod pode processar requests independentes
- Suporta horizontal scaling automático

### Performance
- **Throughput**: ~300 registros/segundo (com 4 threads)
- **Memória**: ~200MB por bulk de 60.000 registros
- **Timeout**: 10 minutos para 60.000 registros

### Limites
- Máximo de 60.000 registros por requisição
- Timeout de 10 minutos para distributed lock
- 4 threads paralelos (ajustável)

## Tratamento de Falhas

### Cenário 1: OpenSearch Indisponível
- Retry automático: até 5 tentativas
- Fallback após 3 falhas no bulk
- Retorna resposta parcial com estatísticas

### Cenário 2: Múltiplos Pods
- Lock impede duplicação
- Pod 2 espera liberação do lock
- Retorna status "PROCESSING"

### Cenário 3: Taxa Elevada de Erro
- Circuit breaker abre após 50% de erro
- Novas requisições falham rápido
- Fallback ativa: respostas com status "FALLBACK"

## Exemplo de Produção

```bash
# Gerar 60.000 registros
curl -X POST http://api.sistema.com/veiculos/bulk \
  -H "Content-Type: application/json" \
  -d @bulk_request_60k.json \
  --max-time 600

# Verificar status
curl http://api.sistema.com/veiculos/bulk/status

# Reprocessar com retry
curl -X POST http://api.sistema.com/veiculos/bulk?retry=true \
  -H "Content-Type: application/json" \
  -d @bulk_request_60k.json
```

## Troubleshooting

### Problema: Circuit Breaker aberto
**Solução**: Aguardar 10 segundos para transição para HALF_OPEN

### Problema: Lock timeout
**Solução**: Aguardar 10 minutos ou reprocessar com novo UUID

### Problema: Alto consumo de memória
**Solução**: Reduzir BATCH_SIZE de 500 para 250

## Suporte

Para dúvidas ou problemas, consulte os logs na pasta `/logs/bulk-veiculo.log`

