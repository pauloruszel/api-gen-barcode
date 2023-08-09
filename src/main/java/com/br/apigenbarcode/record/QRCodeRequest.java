package com.br.apigenbarcode.record;

public record QRCodeRequest(
        String texto,
        Integer scale,
        String foreground,
        String background,
        String download,
        String base64) {
}

