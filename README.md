# 🚀 Demos AWS - Sistema de Gerenciamento de Veículos

Uma aplicação Spring Boot para **gerenciamento e indexação de veículos** usando OpenSearch, com suporte a processamento em bulk de até 65.000 registros simultaneamente.

## 📋 O que faz

- ✅ **Cadastrar veículos individualmente** no OpenSearch
- ✅ **Buscar veículos por ID**
- ✅ **Processar milhares de veículos em bulk** (até 65.000 registros)
- ✅ **Locks distribuídos** com Redis para evitar duplicação
- ✅ **Resiliência** com Circuit Breaker e Retry automático
- ✅ **Arquitetura Hexagonal** com separação clara de responsabilidades

## 🛠️ Tecnologias

- **Java 21** + **Spring Boot 3.5.13**
- **OpenSearch 2.11.0** (indexação e busca)
- **Redis 7.2** (locks distribuídos)
- **Resilience4j** (circuit breaker e retry)
- **Docker** (containerização)

---

## 🚀 Como Subir

### 1. Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker + Docker Compose

### 2. Iniciar Serviços
```bash
cd /Users/andersonluizpereira/java/demos_aws

# Iniciar OpenSearch e Redis
docker-compose up -d
```

### 3. Compilar e Executar
```bash
# Compilar
./mvnw clean compile

# Executar
./mvnw spring-boot:run
```

### 4. Verificar
```bash
# Health check
curl http://localhost:8080/api/health

# Stats da aplicação
curl http://localhost:8080/api/stats
```

**✅ Aplicação iniciada com sucesso!**

---

## 📡 API Endpoints

### Veículos

#### Cadastrar veículo individual
```bash
POST /veiculos
Content-Type: application/json

{
  "id": "1",
  "modelo": "Civic",
  "marca": "Honda",
  "ano": 2024
}
```

#### Buscar veículo por ID
```bash
GET /veiculos/{id}
```

#### Processar bulk (até 65.000 registros)
```bash
POST /veiculos/bulk
Content-Type: application/json

{
  "requestId": "bulk-123",
  "veiculos": [
    {"id": "1", "modelo": "Civic", "marca": "Honda", "ano": 2024},
    {"id": "2", "modelo": "Corolla", "marca": "Toyota", "ano": 2023}
  ]
}
```

#### 🔍 **Buscar com Filtros e Paginação (NOVO)**
```bash
GET /veiculos/search?marca=Honda&ano=2024&modelo=Civic&page=1&pageSize=10
```

**Exemplos de uso com curl:**

```bash
# Buscar Honda
curl -s "http://localhost:8080/veiculos/search?marca=Honda&page=1&pageSize=10" | jq '.'

# Buscar Toyota ano 2024
curl -s "http://localhost:8080/veiculos/search?marca=Toyota&ano=2024&page=1&pageSize=10" | jq '.'

# Buscar modelo Civic
curl -s "http://localhost:8080/veiculos/search?modelo=Civic&page=1&pageSize=10" | jq '.'

# Listar tudo (sem filtros)
curl -s "http://localhost:8080/veiculos/search?page=1&pageSize=50" | jq '.'

# Segunda página
curl -s "http://localhost:8080/veiculos/search?marca=Honda&page=2&pageSize=10" | jq '.'
```

**Parâmetros:**
- `marca` (opcional) - Filtrar por marca
- `ano` (opcional) - Filtrar por ano exato
- `modelo` (opcional) - Filtrar por modelo
- `page` (padrão: 1) - Número da página
- `pageSize` (padrão: 10, máx: 100) - Registros por página

**Resposta:**
```json
{
  "veiculos": [
    {
      "id": "1",
      "modelo": "Civic",
      "marca": "Honda",
      "ano": 2024
    }
  ],
  "total": 500,
  "page": 1,
  "pageSize": 10,
  "totalPages": 50
}
```

**Performance:**
- ✅ Indexed queries com OpenSearch
- ✅ Paginação eficiente (from/size)
- ✅ Circuit Breaker para resiliência
- ✅ Retry automático em caso de falha

### OpenSearch

#### Health check
```bash
GET /api/health
```

#### Estatísticas e verificação de registros
```bash
GET /api/stats
```

**Resposta:**
```json
{
  "cluster_health": {...},
  "bulk_verification": {
    "total_documents_indexed": 65000,
    "unique_vehicle_ids": 65000,
    "has_duplicates": false,
    "expected_records": 65000,
    "test_successful": true,
    "duplicates_count": 0
  },
  "test_files": {
    "bulk_data": "bulk_65000.json",
    "test_script": "test_bulk.sh"
  }
}
```

#### Buscar todos os documentos indexados
```bash
POST /api/search/veiculos
Content-Type: application/json

{
  "query": {
    "match_all": {}
  }
}
```

---

## 🧪 Teste com 65.000 Registros

### 1. Executar teste automatizado
```bash
cd /Users/andersonluizpereira/java/demos_aws
./test_bulk.sh
```

### 2. Ou manualmente
```bash
curl -X POST http://localhost:8080/veiculos/bulk \
  -H "Content-Type: application/json" \
  -d @bulk_65000.json
```

### 3. Verificar se todos os registros foram inseridos
```bash
# Ver estatísticas
curl http://localhost:8080/api/stats

# Buscar todos os documentos (deve retornar 65.000)
curl -X POST http://localhost:8080/api/search/veiculos \
  -H "Content-Type: application/json" \
  -d '{"query": {"match_all": {}}}' | jq '.hits.total.value'
```

### Resultado esperado:
- **Total processado:** 65.000
- **Sucessos:** 65.000
- **Erros:** 0
- **Status:** COMPLETED

---

## 📊 Arquivos de Teste

- `bulk_65000.json` - 65.000 registros de teste (6.3 MB)
- `test_bulk.sh` - Script automatizado de teste

---

## 🔧 Configuração

- **Aplicação:** http://localhost:8080
- **OpenSearch:** http://localhost:9200
- **Redis:** localhost:6379
- **Timeouts:** 30 segundos
- **Batch size:** 500 registros
- **Thread pool:** 4 threads

---

## 📝 Logs

```bash
tail -f app.log
```

Procure por:
```
INFO  Bulk processado: Total=65000, Sucessos=65000, Erros=0
```

---

## 🏗️ Arquitetura

```
├── Controllers (REST API)
├── Use Cases (Regras de negócio)
├── Ports (Interfaces)
├── Adapters (Integrações externas)
├── Config (Spring configurations)
└── Domain (Entidades)
```

---

**Última atualização:** Abril 2026
