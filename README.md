# 🚀 Demos AWS Advanced - Sistema de Gerenciamento de Veículos

Uma aplicação Spring Boot robusta para gerenciamento e indexação de veículos usando **OpenSearch** com suporte a processamento em **bulk**, **locks distribuídos** com Redis e **resiliência** com Resilience4j.

## 📋 Funcionalidades

### ✅ Gerenciamento de Veículos
- **Cadastro individual** de veículos
- **Busca por ID** de veículos indexados
- **Processamento em bulk** de múltiplos veículos com tratamento de erros robusto
- Suporte a batches automáticos para otimização de performance

### ✅ Integração com OpenSearch
- **Indexação automática** de documentos
- **Busca avançada** com queries complexas
- **Health check** do cluster OpenSearch
- **Criação dinâmica** de índices com mappings customizados

### ✅ Resiliência e Confiabilidade
- **Circuit Breaker** para proteção contra falhas em cascata
- **Retry automático** com backoff exponencial
- **Locks distribuídos** via Redis para evitar duplicação em múltiplos pods
- **Processamento paralelo** de batches com executors configurados

### ✅ Arquitetura
- **Hexagonal Architecture** com separação clara de camadas
- **Portas e Adaptadores** para baixo acoplamento
- **Configuração centralizada** de resiliência e timeouts

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|------------|--------|----------|
| **Java** | 17 | Linguagem de programação |
| **Spring Boot** | 3.5.13 | Framework web |
| **OpenSearch** | 2.11.0 | Search engine e indexação |
| **Redis** | 7.2 | Locks distribuídos |
| **Resilience4j** | 2.1.0 | Circuit breaker e retry |
| **Maven** | 4.0.0 | Gerenciador de dependências |
| **Docker** | - | Containerização de serviços |

---

## 🚀 Como Subir a Aplicação

### Pré-requisitos

- **Java 17+** instalado
- **Maven 3.8+** instalado
- **Docker e Docker Compose** instalados
- **Git** (opcional)

### Passo 1: Clonar o Repositório

```bash
cd ~/java
git clone <seu-repositorio> demos_aws
cd demos_aws
```

Ou se já estiver no diretório correto:

```bash
cd /Users/andersonluizpereira/java/demos_aws
```

### Passo 2: Iniciar os Serviços com Docker Compose

Suba o **OpenSearch** e **Redis** usando Docker Compose:

```bash
docker-compose up -d
```

Isso irá iniciar:
- **OpenSearch** na porta `9200` (http://localhost:9200)
- **Redis** na porta `6379` (localhost:6379)

Verifique se os containers estão rodando:

```bash
docker-compose ps
```

### Passo 3: Compilar o Projeto

```bash
./mvnw clean compile
```

Ou com Maven instalado globalmente:

```bash
mvn clean compile
```

### Passo 4: Executar a Aplicação

**Opção A: Via Maven**

```bash
./mvnw spring-boot:run
```

**Opção B: Build e Execute JAR**

```bash
./mvnw clean package -DskipTests
java -jar target/demos_aws-0.0.1-SNAPSHOT.jar
```

A aplicação estará disponível em: **http://localhost:8080**

### Passo 5: Verificar a Saúde da Aplicação

```bash
curl http://localhost:8080/api/health
```

Resposta esperada (em JSON):

```json
{
  "status": "green",
  "number_of_nodes": 1,
  "number_of_data_nodes": 1,
  "active_primary_shards": 0,
  "active_shards": 0,
  "relocating_shards": 0,
  "initializing_shards": 0,
  "unassigned_shards": 0,
  "delayed_unassigned_shards": 0,
  "number_of_pending_tasks": 0,
  "number_of_in_flight_fetch": 0,
  "timed_out": false,
  "task_max_waiting_in_millis": 0,
  "active_shards_percent_as_number": 100.0
}
```

---

## 📡 Endpoints da API

### Gerenciamento de Veículos

#### 1. **Cadastrar Veículo Individual**
```bash
POST /veiculos
Content-Type: application/json

{
  "id": "veiculo-001",
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2024,
  "placa": "ABC1234"
}
```

**Resposta:** `200 OK`

#### 2. **Buscar Veículo por ID**
```bash
GET /veiculos/{id}
```

**Exemplo:**
```bash
curl http://localhost:8080/veiculos/veiculo-001
```

**Resposta:**
```json
{
  "id": "veiculo-001",
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2024,
  "placa": "ABC1234"
}
```

#### 3. **Cadastrar Veículos em Bulk**
```bash
POST /veiculos/bulk
Content-Type: application/json

{
  "requestId": "bulk-001",
  "veiculos": [
    {
      "id": "veiculo-001",
      "marca": "Toyota",
      "modelo": "Corolla",
      "ano": 2024,
      "placa": "ABC1234"
    },
    {
      "id": "veiculo-002",
      "marca": "Honda",
      "modelo": "Civic",
      "ano": 2023,
      "placa": "XYZ5678"
    }
  ]
}
```

**Resposta:**
```json
{
  "totalProcessado": 2,
  "sucessos": 2,
  "erros": 0,
  "mensagem": "Processamento concluído com sucesso",
  "status": "COMPLETED",
  "batchStatuses": [
    "Batch 1: 2 sucessos, 0 erros"
  ]
}
```

---

### OpenSearch / Indexação

#### 1. **Verificar Saúde do OpenSearch**
```bash
GET /api/health
```

#### 2. **Criar Índice**
```bash
POST /api/index/{indexName}
Content-Type: application/json

{
  "mappings": {
    "properties": {
      "marca": { "type": "keyword" },
      "modelo": { "type": "text" },
      "ano": { "type": "integer" },
      "placa": { "type": "keyword" }
    }
  }
}
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/api/index/veiculos \
  -H "Content-Type: application/json" \
  -d '{
    "mappings": {
      "properties": {
        "marca": { "type": "keyword" },
        "modelo": { "type": "text" },
        "ano": { "type": "integer" },
        "placa": { "type": "keyword" }
      }
    }
  }'
```

#### 3. **Indexar Documento**
```bash
POST /api/document/{indexName}/{docId}
Content-Type: application/json

{
  "marca": "Toyota",
  "modelo": "Corolla",
  "ano": 2024,
  "placa": "ABC1234"
}
```

#### 4. **Buscar Documentos**
```bash
POST /api/search/{indexName}
Content-Type: application/json

{
  "query": {
    "match": {
      "marca": "Toyota"
    }
  }
}
```

---

## 🔧 Configurações Importantes

### application.properties

```properties
spring.application.name=demos_aws
server.port=8080
```

### Conexão com Serviços

- **OpenSearch:** http://localhost:9200
- **Redis:** localhost:6379
- **Spring Boot App:** http://localhost:8080

### Timeouts

- **Connection Timeout:** 30 segundos
- **Response Timeout:** 30 segundos
- **Lock Timeout:** 300 segundos (5 minutos)

---

## 📊 Arquitetura do Projeto

```
src/main/java/com/comunidade/app/
├── adapters/
│   ├── in/
│   │   └── controller/          # REST Controllers
│   │       ├── VeiculoController.java
│   │       └── OpenSearchController.java
│   └── out/
│       └── client/              # Integrações externas
│           └── OpenSearchService.java
├── application/
│   ├── core/
│   │   ├── domain/              # Entidades de domínio
│   │   │   ├── Veiculo.java
│   │   │   ├── BulkVeiculoRequest.java
│   │   │   └── BulkVeiculoResponse.java
│   │   └── usecase/             # Casos de uso
│   │       ├── VeiculoServiceUseCase.java
│   │       └── BulkVeiculoServiceUseCase.java
│   └── ports/
│       ├── in/                  # Interfaces de entrada
│       └── out/                 # Interfaces de saída
├── config/                      # Configurações do Spring
│   ├── OpenSearchConfig.java
│   ├── ResilienceConfig.java
│   └── RestTemplateConfig.java
└── DemosAwsApplication.java     # Main class
```

---

## 🐛 Troubleshooting

### Problema: OpenSearch não está acessível

```
Failed to check OpenSearch health: Connection refused
```

**Solução:**
```bash
# Verifique se o container está rodando
docker-compose ps

# Reinicie o docker-compose
docker-compose restart opensearch
```

### Problema: Porta 9200 já está em uso

**Solução:**
```bash
# Encontre qual processo está usando a porta
lsof -i :9200

# Ou force parar todos os containers
docker-compose down
docker-compose up -d
```

### Problema: Redis não está conectando

**Solução:**
```bash
# Verifique a conectividade
redis-cli -h localhost -p 6379 ping

# Reinicie o Redis
docker-compose restart redis
```

### Problema: Build Maven falha

**Solução:**
```bash
# Limpe o cache Maven
./mvnw clean

# Execute novamente
./mvnw clean compile
```

---

## 📝 Logs

Os logs da aplicação são salvos em:

```
./app.log
```

Para ver logs em tempo real:

```bash
tail -f app.log
```

---

## 🚦 Status da Aplicação

Após iniciar, verifique:

1. **OpenSearch Health:** `curl http://localhost:9200/_cluster/health`
2. **Redis Connection:** `redis-cli ping` (deve retornar PONG)
3. **App Health:** `curl http://localhost:8080/api/health`

---

## 📚 Documentação Adicional

- [API_GUIDE.md](./API_GUIDE.md) - Guia detalhado de endpoints
- [BULK_GUIDE.md](./BULK_GUIDE.md) - Guia de processamento em bulk
- [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md) - Guia de deploy em produção
- [IMPLEMENTACAO_BULK.md](./IMPLEMENTACAO_BULK.md) - Detalhes técnicos de bulk

---

## 👨‍💻 Desenvolvedor

**Sistema desenvolvido com Spring Boot Hexagonal Architecture**

Mantém-se atualizado com as melhores práticas de:
- ✅ Arquitetura em camadas
- ✅ Padrão Ports & Adapters
- ✅ Resiliência com Resilience4j
- ✅ Processamento assíncrono e paralelo
- ✅ Integração com OpenSearch
- ✅ Locks distribuídos com Redis

---

## 📄 Licença

Este projeto é de código aberto. Consulte a seção de licença para mais informações.

---

**Última atualização:** Abril 2026

