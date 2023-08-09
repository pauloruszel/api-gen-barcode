package com.br.apigenbarcode.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springdoc.core.properties.SwaggerUiConfigProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class SwaggerConfigTest {

    @InjectMocks
    private SwaggerConfig swaggerConfig;

    @Mock
    private SwaggerUiConfigProperties swaggerUiConfigProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(swaggerUiConfigProperties.getShowExtensions()).thenReturn(true);
    }

    @Test
    void testCustomOpenAPI() {
        OpenAPI openAPI = swaggerConfig.customOpenAPI(swaggerUiConfigProperties);
        assertEquals("Serviço de Geração de QR Code", openAPI.getInfo().getTitle());
        assertEquals("API responsável por gerar QR Codes com diversas configurações personalizáveis", openAPI.getInfo().getDescription());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());

        Server server = openAPI.getServers().get(0);
        assertEquals("http://localhost:8080", server.getUrl());
        assertEquals("Ambiente Local", server.getDescription());
    }
}
