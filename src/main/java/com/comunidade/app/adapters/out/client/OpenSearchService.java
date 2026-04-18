package com.comunidade.app.adapters.out.client;

import com.comunidade.app.application.ports.out.OpenSearchServicePort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class OpenSearchService implements OpenSearchServicePort {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "http://localhost:9200";

    public OpenSearchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String checkHealth() {
        try {
            String response = restTemplate.getForObject(baseUrl + "/_cluster/health", String.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check OpenSearch health: " + e.getMessage());
        }
    }

    public String createIndex(String indexName, String mappings) {
        try {
            String url = baseUrl + "/" + indexName;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(mappings, headers);
            String response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class).getBody();
            return response;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400 && e.getResponseBodyAsString().contains("resource_already_exists_exception")) {
                return "{\"acknowledged\":true,\"message\":\"Index already exists\"}";
            } else {
                throw new RuntimeException("Failed to create index: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create index: " + e.getMessage());
        }
    }

    public String indexDocument(String indexName, String documentId, Map<String, Object> document) {
        try {
            String url = baseUrl + "/" + indexName + "/_doc/" + documentId;
            String json = objectMapper.writeValueAsString(document);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            String response = restTemplate.postForObject(url, entity, String.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to index document: " + e.getMessage());
        }
    }

    public String search(String indexName, Map<String, Object> query) {
        try {
            String url = baseUrl + "/" + indexName + "/_search";
            String json = objectMapper.writeValueAsString(query);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            String response = restTemplate.postForObject(url, entity, String.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search: " + e.getMessage());
        }
    }
}
