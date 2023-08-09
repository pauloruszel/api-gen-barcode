package com.br.apigenbarcode.record;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QRCodeRequestTest {

    @Test
    void testCreateQRCodeRequest() {
        String texto = "Test Text";
        Integer scale = 100;
        String foreground = "#FFFFFF";
        String background = "#000000";
        String download = "false";
        String base64 = "false";

        QRCodeRequest request = new QRCodeRequest(texto, scale, foreground, background, download, base64);

        assertEquals(texto, request.texto());
        assertEquals(scale, request.scale());
        assertEquals(foreground, request.foreground());
        assertEquals(background, request.background());
        assertEquals(download, request.download());
        assertEquals(base64, request.base64());
    }
}
