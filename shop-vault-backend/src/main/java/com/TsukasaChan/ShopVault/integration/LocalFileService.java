package com.TsukasaChan.ShopVault.integration;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalFileService {

    @Value("${shop-vault.upload.path:./uploads}")
    private String configuredUploadPath;

    @Value("${shop-vault.upload.url-prefix:/uploads}")
    private String urlPrefix;

    private Path uploadPath;

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

    public String uploadFile(MultipartFile file) {
        return uploadFile(file, "common");
    }

    public String uploadFile(MultipartFile file, String category) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        String relativePath = category + "/" + datePath + "/" + fileName;
        Path fullPath = uploadPath.resolve(relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            File destFile = Objects.requireNonNull(fullPath.toFile(), "无法创建目标文件");
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException("文件上传至本地存储失败: " + e.getMessage(), e);
        }

        return urlPrefix + "/" + relativePath;
    }

    public String uploadAvatar(MultipartFile file, Long userId) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + extension;

        String relativePath = "avatar/" + datePath + "/" + fileName;
        Path fullPath = uploadPath.resolve(relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            File destFile = Objects.requireNonNull(fullPath.toFile(), "无法创建目标文件");
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException("头像上传失败: " + e.getMessage(), e);
        }

        return urlPrefix + "/" + relativePath;
    }

    public String uploadChatImage(MultipartFile file, Long messageId) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = "chat_" + messageId + "_" + System.currentTimeMillis() + extension;

        String relativePath = "chat/" + datePath + "/" + fileName;
        Path fullPath = uploadPath.resolve(relativePath);

        try {
            Files.createDirectories(fullPath.getParent());
            File destFile = Objects.requireNonNull(fullPath.toFile(), "无法创建目标文件");
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException("聊天图片上传失败: " + e.getMessage(), e);
        }

        return urlPrefix + "/" + relativePath;
    }

    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            return false;
        }

        String relativePath = fileUrl.substring(urlPrefix.length() + 1);
        Path fullPath = uploadPath.resolve(relativePath);

        try {
            return Files.deleteIfExists(fullPath);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean fileExists(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(urlPrefix)) {
            return false;
        }

        String relativePath = fileUrl.substring(urlPrefix.length() + 1);
        Path fullPath = uploadPath.resolve(relativePath);

        return Files.exists(fullPath);
    }
}
