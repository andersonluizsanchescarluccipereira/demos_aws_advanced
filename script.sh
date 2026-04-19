#!/usr/bin/env bash

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

  # Monta querystring apenas com parâmetros informados
  qs="page=$page&pageSize=$size"
  [[ -n "$marca"  ]] && qs="$qs&marca=$marca"
  [[ -n "$ano"    ]] && qs="$qs&ano=$ano"
  [[ -n "$modelo" ]] && qs="$qs&modelo=$modelo"

  curl -s "$BASE_URL/veiculos/search?$qs" | print_json
}

deletar_por_id() {
  echo "➡️  Deletar por ID"
  read -rp "ID: " id

  # Tenta endpoint REST da sua API (se existir)
  resp=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/veiculos/$id")

  if [[ "$resp" == "200" || "$resp" == "204" ]]; then
    echo "✔️  Deletado via API."
    return
  fi

  echo "⚠️  Endpoint DELETE /veiculos/{id} não disponível. Tentando direto no OpenSearch..."

  # Fallback direto no OpenSearch
  curl -s -X DELETE "http://localhost:9200/$INDEX/_doc/$id" | print_json
}

deletar_tudo() {
  echo "⚠️  ATENÇÃO: isso vai apagar TODOS os documentos do índice '$INDEX'."
  read -rp "Confirma? (y/N): " c
  [[ "$c" != "y" && "$c" != "Y" ]] && echo "Cancelado." && return

  # Preferir API se existir
  resp=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/veiculos")

  if [[ "$resp" == "200" || "$resp" == "204" ]]; then
    echo "✔️  Base limpa via API."
    return
  fi

  echo "⚠️  Endpoint DELETE /veiculos não disponível. Limpando via OpenSearch (delete_by_query)..."

  curl -s -X POST "http://localhost:9200/$INDEX/_delete_by_query" \
    -H "Content-Type: application/json" \
    -d '{"query":{"match_all":{}}}' | print_json
}

deduplicar() {
  echo "➡️  Deduplicar (marca+modelo+ano)"
  echo "Vai manter 1 doc por combinação e remover duplicados."

  read -rp "Confirma? (y/N): " c
  [[ "$c" != "y" && "$c" != "Y" ]] && echo "Cancelado." && return

  # Busca todos (pode ajustar size/scroll se tiver muitos registros)
  echo "🔎 Buscando documentos..."
  resp=$(curl -s -X POST "http://localhost:9200/$INDEX/_search" \
    -H "Content-Type: application/json" \
    -d '{
      "size": 10000,
      "_source": ["id","marca","modelo","ano"],
      "query": { "match_all": {} }
    }')

  if has_jq; then
    ids_to_delete=$(echo "$resp" | jq -r '
      .hits.hits
      | map({id: ._id, k: (. _source.marca + "|" + ._source.modelo + "|" + (. _source.ano|tostring))})
      | group_by(.k)
      | map(.[1:][])     # pega todos exceto o primeiro de cada grupo
      | .[].id
    ')
  else
    echo "❌ Para deduplicar é necessário ter o 'jq' instalado."
    return
  fi

  if [[ -z "$ids_to_delete" ]]; then
    echo "✔️  Nenhum duplicado encontrado."
    return
  fi

  echo "🗑️  Removendo duplicados..."
  for id in $ids_to_delete; do
    curl -s -X DELETE "http://localhost:9200/$INDEX/_doc/$id" >/dev/null
  done

  echo "✔️  Deduplicação concluída."
}

health() {
  echo "➡️  Health"
  curl -s "$BASE_URL/api/health" | print_json
}

stats() {
  echo "➡️  Stats"
  curl -s "$BASE_URL/api/stats" | print_json
}

# ========= MENU =========

menu() {
  clear
  echo "=============================="
  echo "   CRUD Veículos (Menu CLI)   "
  echo "=============================="
  echo "1) Cadastrar veículo"
  echo "2) Listar (paginado)"
  echo "3) Buscar por ID"
  echo "4) Buscar com filtros (marca/ano/modelo)"
  echo "5) Deletar por ID"
  echo "6) Deletar TODOS"
  echo "7) Deduplicar (marca+modelo+ano)"
  echo "8) Health"
  echo "9) Stats"
  echo "0) Sair"
  echo "------------------------------"
  read -rp "Escolha uma opção: " op

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
    0) exit 0 ;;
    *) echo "Opção inválida" ;;
  esac

  pause
}

# Loop
while true; do
  menu
done