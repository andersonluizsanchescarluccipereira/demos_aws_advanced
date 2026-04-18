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
}
