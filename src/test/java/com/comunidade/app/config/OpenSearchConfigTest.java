//package com.comunidade.app.config;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.opensearch.client.opensearch.OpenSearchClient;
//import org.springframework.web.client.RestTemplate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class OpenSearchConfigTest {
//
//    private OpenSearchConfig config;
//
//    @BeforeEach
//    void setUp() {
//        config = new OpenSearchConfig();
//    }
//
//    @Test
//    void testRestTemplateBeanCreation() {
//        RestTemplate restTemplate = config.restTemplate();
//
//        assertNotNull(restTemplate);
//    }
//
//    @Test
//    void testOpenSearchClientBeanCreation() {
//        OpenSearchClient client = config.openSearchClient();
//
//        assertNotNull(client);
//    }
//
//    @Test
//    void testRestTemplateIsNotNull() {
//        RestTemplate restTemplate1 = config.restTemplate();
//        RestTemplate restTemplate2 = config.restTemplate();
//
//        assertNotNull(restTemplate1);
//        assertNotNull(restTemplate2);
//    }
//
//    @Test
//    void testOpenSearchClientIsNotNull() {
//        OpenSearchClient client1 = config.openSearchClient();
//        OpenSearchClient client2 = config.openSearchClient();
//
//        assertNotNull(client1);
//        assertNotNull(client2);
//    }
//
//    @Test
//    void testConfigCanBeInstantiated() {
//        OpenSearchConfig config1 = new OpenSearchConfig();
//        OpenSearchConfig config2 = new OpenSearchConfig();
//
//        assertNotNull(config1);
//        assertNotNull(config2);
//    }
//}
//
