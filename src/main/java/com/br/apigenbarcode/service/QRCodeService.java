package com.br.apigenbarcode.service;

import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

import static com.google.zxing.BarcodeFormat.QR_CODE;
import static com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream;
import static java.awt.Color.decode;
import static java.util.Base64.getEncoder;

@Service
@Slf4j
public class QRCodeService {

    public byte[] gerarQRCode(final String texto, final Integer scale, final String foreground, final String background) throws Exception {
        log.info("gerando QR Code com texto: {}, scale: {}, foreground: {}, background: {}", texto, scale, foreground, background);
        validarParametros(texto, scale, foreground, background);

        final var matrix = new MultiFormatWriter().encode(texto, QR_CODE, scale, scale);
        final var config = new MatrixToImageConfig(decode(foreground).getRGB(), decode(background).getRGB());
        final var baos = new ByteArrayOutputStream();
        writeToStream(matrix, "PNG", baos, config);

        log.info("QR Code gerado com sucesso");
        return baos.toByteArray();
    }

    public String encodeBase64(final byte[] image) {
        log.info("Codificando imagem em Base64");
        return getEncoder().encodeToString(image);
    }

    private void validarParametros(final String texto, final Integer scale, final String foreground, final String background) {
        log.info("inicio das validacoes dos parametros com texto: {}, scale: {}, foreground: {}, background: {}", texto, scale, foreground, background);
        if (StringUtils.isBlank(texto)) {
            log.error("Texto nao pode ser vazio");
            throw new IllegalArgumentException("Texto não pode ser vazio");
        }
        if (scale == null || scale <= 0) {
            log.error("Scale deve ser maior que 0");
            throw new IllegalArgumentException("Scale deve ser maior que 0");
        }
        if (isValidHexColor(foreground)) {
            log.error("Foreground deve ser uma cor hexadecimal valida");
            throw new IllegalArgumentException("Foreground deve ser uma cor hexadecimal válida");
        }
        if (isValidHexColor(background)) {
            log.error("Background deve ser uma cor hexadecimal valida");
            throw new IllegalArgumentException("Background deve ser uma cor hexadecimal válida");
        }
        log.info("fim das validacoes dos parametros com texto: {}, scale: {}, foreground: {}, background: {}", texto, scale, foreground, background);
    }

    private boolean isValidHexColor(final String color) {
        return color == null || !color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }
}
