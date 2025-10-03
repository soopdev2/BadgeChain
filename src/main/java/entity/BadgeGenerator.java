package entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BadgeGenerator {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Badge generate(String name, String description, String issuer, String recipient,
            File outImage, File outJson) throws Exception {
        String id = "badge-" + UUID.randomUUID();
        String issuedOn = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        // 1. Genera immagine badge semplice
        BufferedImage img = new BufferedImage(300, 150, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 300, 150);
        g.setColor(Color.BLUE);
        g.drawString("Badge: " + name, 20, 50);
        g.drawString("Issuer: " + issuer, 20, 80);
        g.dispose();
        ImageIO.write(img, "png", outImage);

        // 2. Crea oggetto Badge
        Badge badge = new Badge();
        badge.id = id;
        badge.name = name;
        badge.description = description;
        badge.issuer = issuer;
        badge.recipient = recipient;
        badge.imageUrl = "/badges/" + outImage.getName();
        badge.issuedOn = issuedOn;

        // 3. Serializza come JSON-LD (semplificato)
        String json = "{\n"
                + "  \"@context\": [\"https://w3id.org/openbadges/v3/context.jsonld\"],\n"
                + "  \"type\": [\"BadgeClass\", \"VerifiableCredential\"],\n"
                + "  \"id\": \"" + id + "\",\n"
                + "  \"issuer\": {\"id\": \"" + issuer + "\", \"name\": \"" + issuer + "\"},\n"
                + "  \"issuanceDate\": \"" + issuedOn + "\",\n"
                + "  \"credentialSubject\": {\"id\": \"" + recipient + "\", \"name\": \"" + name + "\"},\n"
                + "  \"image\": \"" + badge.imageUrl + "\"\n"
                + "}";
        Files.write(outJson.toPath(), json.getBytes(StandardCharsets.UTF_8));

        // 4. Calcola hash SHA-256 del JSON
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(json.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        badge.jsonHash = sb.toString();

        return badge;
    }
}
