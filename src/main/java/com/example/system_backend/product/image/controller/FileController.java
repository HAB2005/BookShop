package com.example.system_backend.product.image.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.system_backend.product.image.application.service.FileStorageService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Serve uploaded files URL pattern:
     * /api/files/products/{productId}/{filename}
     */
    @GetMapping("/products/{productId}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable Integer productId,
            @PathVariable String filename) {

        try {
            // Construct relative path
            String relativePath = String.format("products/%d/%s", productId, filename);

            // Check if file exists
            if (!fileStorageService.fileExists(relativePath)) {
                return ResponseEntity.notFound().build();
            }

            // Get file path and create resource
            Path filePath = fileStorageService.getFilePath(relativePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = determineContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Malformed URL for file: products/{}/{}", productId, filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error serving file: products/{}/{}", productId, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download file (force download instead of inline display)
     */
    @GetMapping("/products/{productId}/{filename:.+}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer productId,
            @PathVariable String filename) {

        try {
            // Construct relative path
            String relativePath = String.format("products/%d/%s", productId, filename);

            // Check if file exists
            if (!fileStorageService.fileExists(relativePath)) {
                return ResponseEntity.notFound().build();
            }

            // Get file path and create resource
            Path filePath = fileStorageService.getFilePath(relativePath);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Malformed URL for file download: products/{}/{}", productId, filename, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error downloading file: products/{}/{}", productId, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file info
     */
    @GetMapping("/products/{productId}/{filename:.+}/info")
    public ResponseEntity<Object> getFileInfo(
            @PathVariable Integer productId,
            @PathVariable String filename) {

        try {
            String relativePath = String.format("products/%d/%s", productId, filename);

            if (!fileStorageService.fileExists(relativePath)) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = fileStorageService.getFilePath(relativePath);

            return ResponseEntity.ok(Map.of(
                    "filename", filename,
                    "productId", productId,
                    "path", relativePath,
                    "size", Files.size(filePath),
                    "contentType", determineContentType(filename),
                    "lastModified", Files.getLastModifiedTime(filePath).toString()));

        } catch (IOException e) {
            log.error("Error getting file info: products/{}/{}", productId, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== PRIVATE HELPER METHODS =====
    private String determineContentType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" ->
                "image/jpeg";
            case "png" ->
                "image/png";
            case "gif" ->
                "image/gif";
            case "webp" ->
                "image/webp";
            default ->
                "application/octet-stream";
        };
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
