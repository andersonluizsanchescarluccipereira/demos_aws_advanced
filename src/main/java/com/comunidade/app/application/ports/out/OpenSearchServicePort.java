package com.comunidade.app.application.ports.out;

import java.util.Map;

public interface OpenSearchServicePort {
    String checkHealth();
    String createIndex(String indexName, String mappings);
    String indexDocument(String indexName, String documentId, Map<String, Object> document);
    String search(String indexName, Map<String, Object> query);
}
