package com.br.apigenbarcode.controller;

import com.br.apigenbarcode.record.QRCodeRequest;
import com.br.apigenbarcode.service.QRCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class QRCodeControllerTest {

    @InjectMocks
    private QRCodeController qrCodeController;

    @Mock
    private QRCodeService qrCodeService;

    private QRCodeRequest qrCodeRequest;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "false", "false");
        byte[] qrCode = "test".getBytes();
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyString(), anyString())).thenReturn(qrCode);
        when(qrCodeService.encodeBase64(any())).thenReturn("dGVzdA==");
    }

    @Test
    void testGenerateQRCodeWithBase64() throws Exception {
        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "false", "true");

        Mono<ResponseEntity<?>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> {
                    Map<String, String> responseBody = (Map<String, String>) res.getBody();
                    assertEquals("dGVzdA==", responseBody.get("image"));
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
    void testGenerateQRCodeWithoutBase64AndDownload() throws Exception {
        Mono<ResponseEntity<?>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> assertArrayEquals("test".getBytes(), (byte[]) res.getBody()))
                .verifyComplete();
    }

}