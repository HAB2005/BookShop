package com.example.system_backend.product.image.application.service;

import com.example.system_backend.common.config.FileUploadProperties;
import com.example.system_backend.common.exception.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileUploadProperties fileUploadProperties;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    /**
     * Store uploaded file and return the file path
     */
    public String storeFile(MultipartFile file, Integer productId) {
        validateFile(file);

        try {
            // Create directory structure: uploads/products/{productId}/
            Path productDir = createProductDirectory(productId);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(fileExtension);

            // Full file path
            Path filePath = productDir.resolve(uniqueFilename);

            // Copy file to destination
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for database storage
            String relativePath = String.format("products/%d/%s", productId, uniqueFilename);

            log.info("File stored successfully: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store file for product {}: {}", productId, e.getMessage());
            throw new SystemException("Failed to store file", "FILE_STORAGE_ERROR");
        }
    }

    /**
     * Delete file from storage
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(fileUploadProperties.getDir()).resolve(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", relativePath);
            } else {
                log.warn("File not found for deletion: {}", relativePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", relativePath, e.getMessage());
            throw new SystemException("Failed to delete file", "FILE_DELETE_ERROR");
        }
    }

    /**
     * Get full file path for serving
     */
    public Path getFilePath(String relativePath) {
        return Paths.get(fileUploadProperties.getDir()).resolve(relativePath);
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String relativePath) {
        Path filePath = Paths.get(fileUploadProperties.getDir()).resolve(relativePath);
        return Files.exists(filePath);
    }

    // ===== PRIVATE HELPER METHODS =====
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > fileUploadProperties.getMaxFileSize()) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d bytes",
                            fileUploadProperties.getMaxFileSize()));
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private Path createProductDirectory(Integer productId) throws IOException {
        Path productDir = Paths.get(fileUploadProperties.getDir(), "products", productId.toString());

        if (!Files.exists(productDir)) {
            Files.createDirectories(productDir);
            log.info("Created directory: {}", productDir);
        }

        return productDir;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }
}
