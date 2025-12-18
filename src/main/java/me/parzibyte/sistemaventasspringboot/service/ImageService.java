package me.parzibyte.sistemaventasspringboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.uploads.dir:uploads}") // configurable en application.properties
    private String uploadsDir;

    private Path ROOT;

    @PostConstruct
    void init() throws IOException {
        ROOT = Path.of(uploadsDir).toAbsolutePath().normalize();
        if (!Files.exists(ROOT)) Files.createDirectories(ROOT);
    }

    /** Guarda la imagen como JPG con fondo blanco y retorna la URL pública (/uploads/xxx.jpg). */
    public String saveWithWhiteBg(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        BufferedImage src = ImageIO.read(file.getInputStream());
        if (src == null) return null;

        // Lienzo blanco (sin transparencia)
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, dst.getWidth(), dst.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();

        String filename = UUID.randomUUID() + ".jpg";
        File out = ROOT.resolve(filename).toFile();
        ImageIO.write(dst, "jpg", out);

        // Debe coincidir con el handler de WebConfig (/uploads/**)
        return "/uploads/" + filename;
    }
}
