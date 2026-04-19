#!/bin/bash

# Script para testar o bulk de 65000 registros
# Certifique-se de que a aplicação está rodando em http://localhost:8080

echo "========================================="
echo "🚀 TESTE DE BULK COM 65000 REGISTROS"
echo "========================================="
echo ""

# Verificar se o arquivo existe
if [ ! -f "bulk_65000.json" ]; then
    echo "❌ Erro: Arquivo bulk_65000.json não encontrado!"
    exit 1
fi

echo "📦 Enviando 65000 registros para o servidor..."
echo "🌐 Endpoint: http://localhost:8080/veiculos/bulk"
echo ""

# Enviar a requisição
START_TIME=$(date +%s)

RESPONSE=$(curl -s -X POST http://localhost:8080/veiculos/bulk \
  -H "Content-Type: application/json" \
  -d @bulk_65000.json)

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo "✅ Resposta recebida em ${DURATION}s"
echo ""
echo "📊 Resultado:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""
echo "========================================="
echo "✨ Teste concluído!"
echo "========================================="

