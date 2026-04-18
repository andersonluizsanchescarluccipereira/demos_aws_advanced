# OpenSearch Demo API

## Endpoints Disponíveis

### 1. Health Check
```
GET /api/health
```
Verifica a saúde do cluster OpenSearch.

**Exemplo:**
```bash
curl http://localhost:8080/api/health
```

### 2. Criar Índice
```
POST /api/index/{indexName}
Content-Type: application/json
```

**Body (JSON Mappings):**
```json
{
  "mappings": {
    "properties": {
      "modelo": { "type": "text" },
      "marca": { "type": "keyword" },
      "ano": { "type": "integer" }
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
        "modelo": { "type": "text" },
        "marca": { "type": "keyword" },
        "ano": { "type": "integer" }
      }
    }
  }'
```

### 3. Indexar Documento
```
POST /api/document/{indexName}/{docId}
Content-Type: application/json
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/api/document/veiculos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "modelo": "Civic",
    "marca": "Honda",
    "ano": 2022
  }'
```

### 4. Buscar Documentos
```
POST /api/search/{indexName}
Content-Type: application/json
```

**Body (Query DSL):**
```json
{
  "query": {
    "match": {
      "marca": "Honda"
    }
  }
}
```

**Exemplo:**
```bash
curl -X POST http://localhost:8080/api/search/veiculos \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "match": {
        "marca": "Honda"
      }
    }
  }'
```

## Setup

1. Inicie o OpenSearch via Docker Compose:
```bash
docker-compose up -d
```

2. Execute o script de setup:
```bash
sh ./script.sh
```

3. Inicie a aplicação Spring Boot:
```bash
./mvnw spring-boot:run
```

4. A API estará disponível em: `http://localhost:8080`
