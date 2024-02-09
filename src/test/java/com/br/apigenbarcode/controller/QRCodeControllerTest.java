package com.br.apigenbarcode.controller;

import com.br.apigenbarcode.record.QRCodeRequest;
import com.br.apigenbarcode.service.QRCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class QRCodeControllerTest {

    @InjectMocks
    private QRCodeController qrCodeController;

    @Mock
    private QRCodeService qrCodeService;

    private QRCodeRequest qrCodeRequest;

    @BeforeEach
    void setUp() throws Exception {
        openMocks(this);

        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "false", "false");
        byte[] qrCode = "test".getBytes();
        when(qrCodeService.gerarQRCode(anyString(), anyInt(), anyString(), anyString())).thenReturn(qrCode);
        when(qrCodeService.encodeBase64(any())).thenReturn("dGVzdA==");
    }

    @Test
    void testGenerateQRCodeWithBase64() throws Exception {
        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "false", "true");

        Mono<ResponseEntity<?>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> {
                    assertEquals(HttpStatus.OK, res.getStatusCode());
                    assertInstanceOf(Map.class, res.getBody());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseBody = (Map<String, Object>) res.getBody();
                    assertTrue(responseBody.containsKey("image"));
                    Object imageValue = responseBody.get("image");
                    assertInstanceOf(String.class, imageValue);
                    assertEquals("dGVzdA==", imageValue);
                })
                .verifyComplete();
    }


    @Test
    void testGenerateQRCodeWithDownload() throws Exception {
        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "true", "false");

        Mono<ResponseEntity<?>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> assertArrayEquals("test".getBytes(), (byte[]) res.getBody()))
                .verifyComplete();
    }


    @Test
    void testGenerateQRCodeWithBase64AndDownloadAreFalse() throws Exception {
        Mono<ResponseEntity<?>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> {
                    assertEquals(HttpStatus.OK, res.getStatusCode());
                    assertInstanceOf(Map.class, res.getBody());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseBody = (Map<String, Object>) res.getBody();
                    assertTrue(responseBody.containsKey("error"));
                    Object errorMessage = responseBody.get("error");
                    assertInstanceOf(String.class, errorMessage);
                    assertEquals("Pelo menos uma das opções download ou base64 deve ser verdadeira.", errorMessage);
                })
                .verifyComplete();
    }

    @Test
    void testGenerateQRCodeWithBase64AndDownloadAreTrue() throws Exception {
        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "true", "true");

        Mono<ResponseEntity<?>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> {
                    assertEquals(HttpStatus.OK, res.getStatusCode());
                    assertInstanceOf(Map.class, res.getBody());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseBody = (Map<String, Object>) res.getBody();
                    assertTrue(responseBody.containsKey("error"));
                    Object errorMessage = responseBody.get("error");
                    assertInstanceOf(String.class, errorMessage);
                    assertEquals("Apenas uma das opções download ou base64 deve ser verdadeira.", errorMessage);
                })
                .verifyComplete();
    }




}