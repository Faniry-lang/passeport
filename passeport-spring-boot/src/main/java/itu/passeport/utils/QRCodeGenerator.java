package itu.passeport.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class QRCodeGenerator {

    /**
     * Génère un QR code PNG à partir d'un lien et le stocke dans le dossier donné.
     * Retourne le chemin du fichier généré (chemin absolu).
     */
    public static String generateQRCode(String link, String folderPath) throws IOException, WriterException {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("link must not be null or blank");
        }
        if (folderPath == null || folderPath.isBlank()) {
            throw new IllegalArgumentException("folderPath must not be null or blank");
        }

        Path folder = Paths.get(folderPath);
        if (Files.notExists(folder)) {
            Files.createDirectories(folder);
        }

        String fileName = "qrcode_" + Instant.now().toEpochMilli() + ".png";
        Path filePath = folder.resolve(fileName);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(link, BarcodeFormat.QR_CODE, 300, 300);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", filePath);

        return filePath.toAbsolutePath().toString();
    }
}

