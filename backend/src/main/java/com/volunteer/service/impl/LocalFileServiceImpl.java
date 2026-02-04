package com.volunteer.service.impl;

import com.volunteer.service.FileService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
@Slf4j
@Service
public class LocalFileServiceImpl implements FileService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.access-url:/files}")
    private String accessUrl;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadPath);
            log.info("文件上传目录初始化完成: {}", this.uploadPath);
        } catch (IOException e) {
            log.error("无法创建文件上传目录: {}", e.getMessage());
            throw new RuntimeException("无法创建文件上传目录", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null);
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        try {
            // 获取原始文件名
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // 获取文件扩展名
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            // 生成新文件名：UUID + 扩展名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            // 按日期组织目录
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

            // 确定存储路径
            Path targetDir;
            if (StringUtils.hasText(folder)) {
                targetDir = this.uploadPath.resolve(folder).resolve(dateFolder);
            } else {
                targetDir = this.uploadPath.resolve(dateFolder);
            }

            // 创建目录
            Files.createDirectories(targetDir);

            // 目标文件路径
            Path targetFile = targetDir.resolve(newFilename);

            // 保存文件
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            // 生成访问URL
            String relativePath;
            if (StringUtils.hasText(folder)) {
                relativePath = folder + "/" + dateFolder + "/" + newFilename;
            } else {
                relativePath = dateFolder + "/" + newFilename;
            }

            String url = accessUrl + "/" + relativePath;
            log.info("文件上传成功: {} -> {}", originalFilename, url);

            return url;

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }

        try {
            // 从URL提取相对路径
            String relativePath = fileUrl.replace(accessUrl + "/", "");
            Path filePath = this.uploadPath.resolve(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", filePath);
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage());
        }
    }
}
