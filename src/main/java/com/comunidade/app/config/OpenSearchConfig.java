package com.comunidade.app.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.hc.core5.http.HttpHost;

import java.net.URISyntaxException;

@Configuration
public class OpenSearchConfig {

    @Bean
    public OpenSearchClient openSearchClient() throws URISyntaxException {

        HttpHost host = HttpHost.create("http://localhost:9200");

        ApacheHttpClient5TransportBuilder builder =
                ApacheHttpClient5TransportBuilder.builder(host);


        OpenSearchTransport transport = builder.build();

        return new OpenSearchClient(transport);
    }
}