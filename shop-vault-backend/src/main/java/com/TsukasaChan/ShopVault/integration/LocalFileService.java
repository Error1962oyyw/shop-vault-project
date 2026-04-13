package com.TsukasaChan.ShopVault.integration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileService {

    @Value("${shop-vault.upload.path:./uploads}")
    private String configuredUploadPath;

    @Value("${shop-vault.upload.url-prefix:/uploads}")
    private String urlPrefix;

    private Path uploadPath;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );
    
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
            ".exe", ".bat", ".cmd", ".sh", ".php", ".jsp", ".asp", ".aspx",
            ".html", ".js", ".vbs", ".ps1", ".jar", ".class"
    );

    @PostConstruct
    public void init() {
        Path path = Paths.get(configuredUploadPath);
        if (path.isAbsolute()) {
            this.uploadPath = path;
        } else {
            String userDir = System.getProperty("user.dir");
            this.uploadPath = Paths.get(userDir, configuredUploadPath).toAbsolutePath().normalize();
        }
        
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + this.uploadPath, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过限制，最大允许10MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("文件名不能为空");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (extension.isEmpty()) {
            throw new RuntimeException("文件必须包含扩展名");
        }
        
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            log.warn("尝试上传危险文件类型: {}", extension);
            throw new RuntimeException("不支持的文件类型");
        }
        
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("只支持图片文件格式: jpg, jpeg, png, gif, webp, bmp");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("文件内容类型不支持，只允许图片文件");
        }
        
        try {
            validateFileContent(file, extension);
        } catch (IOException e) {
            log.error("文件内容验证失败", e);
            throw new RuntimeException("文件内容验证失败");
        }
    }
    
    private void validateFileContent(MultipartFile file, String extension) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);
            
            if (bytesRead < 2) {
                throw new RuntimeException("文件内容无效");
            }
            
            String magicNumber = bytesToHex(header);
            
            boolean validMagic = switch (extension) {
                case ".jpg", ".jpeg" -> magicNumber.startsWith("FFD8FF");
                case ".png" -> magicNumber.startsWith("89504E47");
                case ".gif" -> magicNumber.startsWith("47494638");
                case ".webp" -> magicNumber.contains("52494646") && magicNumber.contains("57454250");
                case ".bmp" -> magicNumber.startsWith("424D");
                default -> false;
            };
            
            if (!validMagic) {
                log.warn("文件魔数不匹配，可能为伪装文件。扩展名: {}, 魔数: {}", extension, magicNumber);
                throw new RuntimeException("文件内容与扩展名不匹配，可能为伪装文件");
            }
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "common");
    }

    public String uploadFile(MultipartFile file, String category) {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(Objects.requireNonNull(originalFilename)).toLowerCase();

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        String relativePath = sanitizePath(category) + "/" + datePath + "/" + fileName;
        Path fullPath = uploadPath.resolve(relativePath).normalize();
        
        if (!fullPath.startsWith(uploadPath)) {
            throw new RuntimeException("非法的文件路径");
        }

        try {
            Files.createDirectories(fullPath.getParent());
            File destFile = Objects.requireNonNull(fullPath.toFile(), "无法创建目标文件");
            file.transferTo(destFile);
            log.info("文件上传成功: {}", relativePath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败，请稍后重试");
        }

        return urlPrefix + "/" + relativePath;
    }
    
    private String sanitizePath(String path) {
        return path.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public String uploadAvatar(MultipartFile file, Long userId) {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(Objects.requireNonNull(originalFilename)).toLowerCase();

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;

        String relativePath = "avatar/" + datePath + "/" + fileName;
        Path fullPath = uploadPath.resolve(relativePath).normalize();
        
        if (!fullPath.startsWith(uploadPath)) {
            throw new RuntimeException("非法的文件路径");
        }

        try {
            Files.createDirectories(fullPath.getParent());
            File destFile = Objects.requireNonNull(fullPath.toFile(), "无法创建目标文件");
            file.transferTo(destFile);
            log.info("头像上传成功: userId={}, path={}", userId, relativePath);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new RuntimeException("头像上传失败，请稍后重试");
        }

        return urlPrefix + "/" + relativePath;
    }

    public String uploadChatImage(MultipartFile file, Long messageId) {
        validateFile(file);
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(Objects.requireNonNull(originalFilename)).toLowerCase();

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = "chat_" + messageId + "_" + System.currentTimeMillis() + extension;

        String relativePath = "chat/" + datePath + "/" + fileName;
        Path fullPath = uploadPath.resolve(relativePath).normalize();
        
        if (!fullPath.startsWith(uploadPath)) {
            throw new RuntimeException("非法的文件路径");
        }

        try {
            Files.createDirectories(fullPath.getParent());
            File destFile = Objects.requireNonNull(fullPath.toFile(), "无法创建目标文件");
            file.transferTo(destFile);
            log.info("聊天图片上传成功: messageId={}, path={}", messageId, relativePath);
        } catch (IOException e) {
            log.error("聊天图片上传失败", e);
            throw new RuntimeException("聊天图片上传失败，请稍后重试");
        }

        return urlPrefix + "/" + relativePath;
    }

    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            return false;
        }

        String relativePath = fileUrl.substring(urlPrefix.length() + 1);
        Path fullPath = uploadPath.resolve(relativePath).normalize();
        
        if (!fullPath.startsWith(uploadPath)) {
            log.warn("尝试删除非法路径的文件: {}", fileUrl);
            return false;
        }

        try {
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileUrl, e);
            return false;
        }
    }

    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            return false;
        }

        String relativePath = fileUrl.substring(urlPrefix.length() + 1);
        Path fullPath = uploadPath.resolve(relativePath).normalize();
        
        if (!fullPath.startsWith(uploadPath)) {
            return false;
        }

        return Files.exists(fullPath);
    }
}
