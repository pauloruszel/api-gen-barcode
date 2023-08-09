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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

class QRCodeControllerTest {

    @InjectMocks
    private QRCodeController qrCodeController;

    @Mock
    private QRCodeService qrCodeService;

    private QRCodeRequest qrCodeRequest;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this); // Inicializa os mocks

        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "false", "false");
        byte[] qrCode = "test".getBytes();
        when(qrCodeService.generateQRCode(anyString(), anyInt(), anyString(), anyString())).thenReturn(qrCode);
        when(qrCodeService.encodeBase64(any())).thenReturn("dGVzdA==");
    }

    @Test
    void testGenerateQRCodeWithBase64() throws Exception {
        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "false", "true");

        Mono<ResponseEntity<byte[]>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> assertArrayEquals(("{\"image\":\"" + "dGVzdA==" + Arrays.toString("\"}".getBytes())).getBytes(), res.getBody()))
                .verifyComplete();
    }

    @Test
    void testGenerateQRCodeWithDownload() throws Exception {
        qrCodeRequest = new QRCodeRequest("test", 100, "#FFFFFF", "#000000", "true", "false");

        Mono<ResponseEntity<byte[]>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> assertArrayEquals("test".getBytes(), res.getBody()))
                .verifyComplete();
    }

    @Test
    void testGenerateQRCodeWithoutBase64AndDownload() throws Exception {
        Mono<ResponseEntity<byte[]>> response = qrCodeController.generateQRCode(qrCodeRequest);
        StepVerifier.create(response)
                .assertNext(res -> assertArrayEquals("test".getBytes(), res.getBody()))
                .verifyComplete();
    }
}
