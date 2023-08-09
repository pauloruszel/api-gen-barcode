package com.br.apigenbarcode.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;

import static com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream;
import static java.util.Base64.getEncoder;

@Service
@Slf4j
public class QRCodeService {

    public byte[] generateQRCode(String texto, Integer scale, String foreground, String background) throws Exception {
        log.info("Gerando QR Code com texto: {}, scale: {}, foreground: {}, background: {}", texto, scale, foreground, background);
        validateParameters(texto, scale, foreground, background);

        BitMatrix matrix = new MultiFormatWriter().encode(texto, BarcodeFormat.QR_CODE, scale, scale);
        MatrixToImageConfig config = new MatrixToImageConfig(Color.decode(foreground).getRGB(), Color.decode(background).getRGB());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeToStream(matrix, "PNG", baos, config);

        log.info("QR Code gerado com sucesso");
        return baos.toByteArray();
    }

    public String encodeBase64(byte[] image) {
        log.info("Codificando imagem em Base64");
        return getEncoder().encodeToString(image);
    }

    private void validateParameters(String texto, Integer scale, String foreground, String background) {
        log.info("Validando parametros");
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
    }

    private boolean isValidHexColor(String color) {
        return color == null || !color.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    }
}
