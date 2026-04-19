#!/usr/bin/env bash

set -euo pipefail

BASE_URL="http://localhost:8080"
INDEX="veiculos"

has_jq() { command -v jq >/dev/null 2>&1; }

print_json() {
  if has_jq; then jq '.'; else cat; fi
}

pause() {
  echo -e "\nPressione ENTER para continuar..."
  read -r
}

urlencode() {
  python3 -c "import urllib.parse, sys; print(urllib.parse.quote(sys.argv[1]))" "$1"
}

# ========= AÇÕES =========

cadastrar() {
  echo "➡️  Cadastro de veículo"
  read -rp "ID: " id
  read -rp "Modelo: " modelo
  read -rp "Marca: " marca
  read -rp "Ano: " ano

  curl -s -X POST "$BASE_URL/veiculos" \
    -H "Content-Type: application/json" \
    -d "{
      \"id\": \"$id\",
      \"modelo\": \"$modelo\",
      \"marca\": \"$marca\",
      \"ano\": $ano
    }" | print_json

  echo -e "\n✔️  Enviado."
}

listar_paginado() {
  echo "➡️  Listar (paginado)"
  read -rp "Page (default 1): " page
  read -rp "PageSize (default 10): " size
  page=${page:-1}
  size=${size:-10}

  curl -s "$BASE_URL/veiculos/search?page=$page&pageSize=$size" | print_json
}

buscar_por_id() {
  echo "➡️  Buscar por ID"
  read -rp "ID: " id
  curl -s "$BASE_URL/veiculos/$id" | print_json
}

buscar_com_filtros() {
  echo "➡️  Buscar com filtros"
  read -rp "Marca (opcional): " marca
  read -rp "Ano (opcional): " ano
  read -rp "Modelo (opcional): " modelo
  read -rp "Page (default 1): " page
  read -rp "PageSize (default 10): " size
  page=${page:-1}
  size=${size:-10}

  qs="page=$page&pageSize=$size"

  [[ -n "$marca"  ]] && qs="$qs&marca=$(urlencode "$marca")"
  [[ -n "$ano"    ]] && qs="$qs&ano=$ano"
  [[ -n "$modelo" ]] && qs="$qs&modelo=$(urlencode "$modelo")"

  curl -s "$BASE_URL/veiculos/search?$qs" | print_json
}

deletar_por_id() {
  echo "➡️  Deletar por ID"
  read -rp "ID: " id

  resp=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/veiculos/$id")

  if [[ "$resp" == "200" || "$resp" == "204" ]]; then
    echo "✔️  Deletado via API."
    return
  fi

  echo "⚠️  Fallback direto no OpenSearch..."

  curl -s -X DELETE "http://localhost:9200/$INDEX/_doc/$id" | print_json
}

deletar_tudo() {
  echo "⚠️  ATENÇÃO: isso vai apagar TODOS os registros."
  read -rp "Confirma? (y/N): " c
  [[ "$c" != "y" && "$c" != "Y" ]] && echo "Cancelado." && return

  curl -s -X POST "http://localhost:9200/$INDEX/_delete_by_query" \
    -H "Content-Type: application/json" \
    -d '{"query":{"match_all":{}}}' | print_json
}

deduplicar() {
  echo "➡️  Deduplicar (marca+modelo+ano)"
  read -rp "Confirma? (y/N): " c
  [[ "$c" != "y" && "$c" != "Y" ]] && echo "Cancelado." && return

  resp=$(curl -s -X POST "http://localhost:9200/$INDEX/_search" \
    -H "Content-Type: application/json" \
    -d '{
      "size": 10000,
      "_source": ["marca","modelo","ano"],
      "query": { "match_all": {} }
    }')

  if ! has_jq; then
    echo "❌ Instale jq para deduplicação."
    return
  fi

  ids=$(echo "$resp" | jq -r '
    .hits.hits
    | map({id: ._id, k: (._source.marca + "|" + ._source.modelo + "|" + (._source.ano|tostring))})
    | group_by(.k)
    | map(.[1:][])
    | .[].id
  ')

  if [[ -z "$ids" ]]; then
    echo "✔️  Nenhum duplicado."
    return
  fi

  echo "🗑️  Removendo duplicados..."
  for id in $ids; do
    curl -s -X DELETE "http://localhost:9200/$INDEX/_doc/$id" >/dev/null
  done

  echo "✔️  Deduplicação concluída."
}

total_registros() {
  echo "➡️  Total de registros"

  curl -s -X GET "http://localhost:9200/$INDEX/_count" \
    -H "Content-Type: application/json" \
    -d '{"query":{"match_all":{}}}' | print_json
}

health() {
  echo "➡️  Health"
  curl -s "$BASE_URL/api/health" | print_json
}

stats() {
  echo "➡️  Stats"
  curl -s "$BASE_URL/api/stats" | print_json
}

# ========= NOVA FUNÇÃO =========

executar_bulk_65000() {
  echo "========================================="
  echo "🚀 TESTE DE BULK COM 65000 REGISTROS"
  echo "========================================="

  if [ ! -f "bulk_65000.json" ]; then
      echo "❌ Arquivo bulk_65000.json não encontrado!"
      return
  fi

  echo "📦 Enviando registros..."
  echo "🌐 Endpoint: $BASE_URL/veiculos/bulk"

  START_TIME=$(date +%s)

  RESPONSE=$(curl -s -X POST "$BASE_URL/veiculos/bulk" \
    -H "Content-Type: application/json" \
    -d @bulk_65000.json)

  END_TIME=$(date +%s)
  DURATION=$((END_TIME - START_TIME))

  echo ""
  echo "⏱️ Tempo total: ${DURATION}s"
  echo "📊 Resposta:"

  echo "$RESPONSE" | print_json

  echo ""
  echo "✅ Teste concluído!"
}

# ========= MENU =========

menu() {
  clear
  echo "=============================="
  echo "   CRUD Veículos (CLI)        "
  echo "=============================="
  echo "1) Cadastrar"
  echo "2) Listar (paginado)"
  echo "3) Buscar por ID"
  echo "4) Buscar com filtros"
  echo "5) Deletar por ID"
  echo "6) Deletar TODOS"
  echo "7) Deduplicar"
  echo "8) Health"
  echo "9) Stats"
  echo "10) Total de registros"
  echo "11) Teste Bulk 65000"
  echo "0) Sair"
  echo "------------------------------"
  read -rp "Escolha: " op

  case "$op" in
    1) cadastrar ;;
    2) listar_paginado ;;
    3) buscar_por_id ;;
    4) buscar_com_filtros ;;
    5) deletar_por_id ;;
    6) deletar_tudo ;;
    7) deduplicar ;;
    8) health ;;
    9) stats ;;
    10) total_registros ;;
    11) executar_bulk_65000 ;;
    0) exit 0 ;;
    *) echo "Opção inválida" ;;
  esac

  pause
}

while true; do
  menu
done