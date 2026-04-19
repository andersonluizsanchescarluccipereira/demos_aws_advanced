package com.comunidade.app.adapters.in.controller;

import com.comunidade.app.application.ports.out.OpenSearchServicePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OpenSearchController {

    @Autowired
    private OpenSearchServicePort openSearchService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        try {
            String response = openSearchService.checkHealth();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("OpenSearch is not available: " + e.getMessage());
        }
    }

    @PostMapping("/index/{indexName}")
    public ResponseEntity<String> createIndex(@PathVariable String indexName, @RequestBody String mappings) {
        try {
            String response = openSearchService.createIndex(indexName, mappings);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error creating index: " + e.getMessage());
        }
    }

    @PostMapping("/document/{indexName}/{docId}")
    public ResponseEntity<String> indexDocument(@PathVariable String indexName, @PathVariable String docId, @RequestBody String document) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var docMap = mapper.readValue(document, java.util.Map.class);
            String response = openSearchService.indexDocument(indexName, docId, docMap);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error indexing document: " + e.getMessage());
        }
    }

    @PostMapping("/search/{indexName}")
    public ResponseEntity<String> search(@PathVariable String indexName, @RequestBody String query) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var queryMap = mapper.readValue(query, java.util.Map.class);
            String response = openSearchService.search(indexName, queryMap);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error searching: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<String> getStats() {
        try {
            // Verificar saúde do cluster
            String healthResponse = openSearchService.checkHealth();

            // Buscar todos os documentos no índice 'veiculos' para contar
            String searchQuery = """
            {
              "query": {
                "match_all": {}
              },
              "size": 0,
              "aggs": {
                "total_docs": {
                  "value_count": {
                    "field": "_id"
                  }
                },
                "unique_ids": {
                  "cardinality": {
                    "field": "id.keyword"
                  }
                }
              }
            }
            """;

            String searchResponse = openSearchService.search("veiculos", new com.fasterxml.jackson.databind.ObjectMapper().readValue(searchQuery, java.util.Map.class));

            // Parsear resposta para extrair estatísticas
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var searchResult = mapper.readTree(searchResponse);

            int totalDocs = searchResult.path("aggregations").path("total_docs").path("value").asInt(0);
            int uniqueIds = searchResult.path("aggregations").path("unique_ids").path("value").asInt(0);

            boolean hasDuplicates = totalDocs > uniqueIds && uniqueIds > 0;
            int duplicatesCount = Math.max(0, totalDocs - uniqueIds);

            String statsResponse = String.format("""
            {
              "cluster_health": %s,
              "bulk_verification": {
                "total_documents_indexed": %d,
                "unique_vehicle_ids": %d,
                "has_duplicates": %b,
                "expected_records": 65000,
                "test_successful": %b,
                "duplicates_count": %d
              },
              "test_files": {
                "bulk_data": "bulk_65000.json",
                "test_script": "test_bulk.sh"
              }
            }
            """,
            healthResponse,
            totalDocs,
            uniqueIds,
            hasDuplicates,
            (totalDocs == 65000 && !hasDuplicates),
            duplicatesCount
            );

            return ResponseEntity.ok(statsResponse);
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Error getting stats: " + e.getMessage());
        }
    }
}
