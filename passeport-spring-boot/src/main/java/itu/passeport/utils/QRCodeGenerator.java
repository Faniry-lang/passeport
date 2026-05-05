package itu.passeport.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public final class QRCodeGenerator {

    private static final int QR_SIZE = 300;

    private QRCodeGenerator() {
        // utilitaire
    }

    /**
     * Génère un QR code PNG à partir d'un lien et le stocke dans le dossier donné.
     * Retourne le chemin du fichier généré (chemin absolu).
     */
    public static String generateQRCode(String link, String folderPath) throws IOException, WriterException {
        return generateQRCode(link, folderPath, "qrcode.png").toAbsolutePath().toString();
    }

    public static Path generateQRCode(String link, String folderPath, String fileName) throws IOException, WriterException {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("link must not be null or blank");
        }
        if (folderPath == null || folderPath.isBlank()) {
            throw new IllegalArgumentException("folderPath must not be null or blank");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be null or blank");
        }

        Path folder = Paths.get(folderPath);
        if (Files.notExists(folder)) {
            Files.createDirectories(folder);
        }

        String normalizedFileName = fileName.toLowerCase().endsWith(".png") ? fileName : fileName + ".png";
        Path filePath = folder.resolve(normalizedFileName);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(link, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ImageIO.write(qrImage, "PNG", filePath.toFile());

        return filePath.toAbsolutePath();
    }
}

