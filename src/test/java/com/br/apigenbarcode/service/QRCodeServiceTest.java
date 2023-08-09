package com.br.apigenbarcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QRCodeServiceTest {

    @InjectMocks
    private QRCodeService qrCodeService;

    @Mock
    private MultiFormatWriter multiFormatWriter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateQRCode() throws Exception {
        String texto = "test";
        Integer scale = 100;
        String foreground = "#FFFFFF";
        String background = "#000000";

        BitMatrix matrix = mock(BitMatrix.class);
        when(multiFormatWriter.encode(anyString(), any(BarcodeFormat.class), anyInt(), anyInt())).thenReturn(matrix);

        byte[] result = qrCodeService.generateQRCode(texto, scale, foreground, background);
        assertNotNull(result);
    }

    @Test
    void testEncodeBase64() {
        byte[] image = "test".getBytes();
        String result = qrCodeService.encodeBase64(image);
        assertEquals("dGVzdA==", result);
    }

    @Test
    void testValidateParametersWithInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQRCode("", 100, "#FFFFFF", "#000000"));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQRCode("test", 0, "#FFFFFF", "#000000"));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQRCode("test", 100, "FFFFFF", "#000000"));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.generateQRCode("test", 100, "#FFFFFF", "000000"));
    }
}

