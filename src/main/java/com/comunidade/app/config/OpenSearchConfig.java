package com.comunidade.app.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.core5.http.HttpHost;

@Configuration
public class OpenSearchConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OpenSearchClient openSearchClient() {
        HttpHost host = new HttpHost("localhost", "http", 9200);
        OpenSearchTransport transport = ApacheHttpClient5TransportBuilder.builder(host).build();
        return new OpenSearchClient(transport);
    }

    // OpenSearch configurado para ser acessado via RestTemplate
    // URL: http://localhost:9200
}
