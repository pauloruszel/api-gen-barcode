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
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
        QRCodeRequest request = new QRCodeRequest("texto", 200, "#000000", "#FFFFFF", "false", "false");
        when(qrCodeService.gerarQRCode(any(), any(), any(), any())).thenReturn(new byte[]{});
        when(qrCodeService.encodeBase64(any())).thenReturn("base64Image");

        StepVerifier.create(qrCodeController.generateQRCode(request))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    void testGenerateQRCodeWithBase64AndDownloadAreTrue() throws Exception {
        QRCodeRequest request = new QRCodeRequest("texto", 200, "#000000", "#FFFFFF", "true", "true");
        when(qrCodeService.gerarQRCode(any(), any(), any(), any())).thenReturn(new byte[]{});
        when(qrCodeService.encodeBase64(any())).thenReturn("base64Image");

        StepVerifier.create(qrCodeController.generateQRCode(request))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == BAD_REQUEST)
                .verifyComplete();
    }

}