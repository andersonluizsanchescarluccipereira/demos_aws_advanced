#!/bin/bash

set -e

INDEX="veiculos"

echo "⏳ Aguardando OpenSearch subir..."
sleep 5

BASE_URL="http://localhost:9200"

echo "✅ Endpoint: $BASE_URL"

# 🔁 espera o endpoint responder
echo "⏳ Aguardando OpenSearch responder..."
until curl -s "$BASE_URL" > /dev/null; do
  echo "⏳ aguardando..."
  sleep 2
done

echo "🔎 Health check (raw)"
curl -s "$BASE_URL/_cluster/health"

echo -e "\n🧱 Criando índice"
curl -s -X PUT "$BASE_URL/$INDEX" -H 'Content-Type: application/json' -d '
{
  "mappings": {
    "properties": {
      "modelo": { "type": "text" },
      "marca":  { "type": "keyword" },
      "ano":    { "type": "integer" }
    }
  }
}' || true

echo -e "\n🚗 Inserindo documento"
curl -s -X POST "$BASE_URL/$INDEX/_doc/1" -H 'Content-Type: application/json' -d '
{
  "modelo": "Civic",
  "marca": "Honda",
  "ano": 2022
}'

echo -e "\n🔍 Buscando por ID"
curl -s "$BASE_URL/$INDEX/_doc/1"

echo -e "\n🔎 Query por marca"
curl -s -X GET "$BASE_URL/$INDEX/_search" -H 'Content-Type: application/json' -d '
{
  "query": {
    "match": {
      "marca": "Honda"
    }
  }
}'

echo -e "\n📊 Listando índices"
curl -s "$BASE_URL/_cat/indices?v"

echo -e "\n✅ Teste finalizado com sucesso!"