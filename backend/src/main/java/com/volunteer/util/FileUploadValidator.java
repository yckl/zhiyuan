package com.volunteer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 文件上传安全验证器
 * 用于验证上传文件的类型、大小和安全性
 */
@Slf4j
public class FileUploadValidator {

    /**
     * 允许上传的图片扩展名（白名单）
     */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg");

    /**
     * 允许上传的文档扩展名（白名单）
     */
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt");

    /**
     * 允许上传的视频扩展名（白名单）
     */
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm");

    /**
     * 所有允许的扩展名
     */
    private static final Set<String> ALL_ALLOWED_EXTENSIONS;
    static {
        ALL_ALLOWED_EXTENSIONS = new java.util.HashSet<>();
        ALL_ALLOWED_EXTENSIONS.addAll(ALLOWED_IMAGE_EXTENSIONS);
        ALL_ALLOWED_EXTENSIONS.addAll(ALLOWED_DOCUMENT_EXTENSIONS);
        ALL_ALLOWED_EXTENSIONS.addAll(ALLOWED_VIDEO_EXTENSIONS);
    }

    /**
     * 默认最大文件大小: 10MB
     */
    private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 图片最大文件大小: 5MB
     */
    private static final long IMAGE_MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 视频最大文件大小: 100MB
     */
    private static final long VIDEO_MAX_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * 验证上传文件（通用）
     */
    public static void validate(MultipartFile file) {
        validateNotEmpty(file);
        String extension = getExtension(file);
        validateExtension(extension, ALL_ALLOWED_EXTENSIONS);
        validateFileSize(file, DEFAULT_MAX_FILE_SIZE);
        validateContentType(file, extension);
    }

    /**
     * 验证图片文件
     */
    public static void validateImage(MultipartFile file) {
        validateNotEmpty(file);
        String extension = getExtension(file);
        validateExtension(extension, ALLOWED_IMAGE_EXTENSIONS);
        validateFileSize(file, IMAGE_MAX_FILE_SIZE);

        // 验证MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("文件不是有效的图片格式");
        }
    }

    /**
     * 验证文档文件
     */
    public static void validateDocument(MultipartFile file) {
        validateNotEmpty(file);
        String extension = getExtension(file);
        validateExtension(extension, ALLOWED_DOCUMENT_EXTENSIONS);
        validateFileSize(file, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 验证视频文件
     */
    public static void validateVideo(MultipartFile file) {
        validateNotEmpty(file);
        String extension = getExtension(file);
        validateExtension(extension, ALLOWED_VIDEO_EXTENSIONS);
        validateFileSize(file, VIDEO_MAX_FILE_SIZE);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new RuntimeException("文件不是有效的视频格式");
        }
    }

    private static void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
    }

    private static String getExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("文件必须有扩展名");
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private static void validateExtension(String extension, Set<String> allowedExtensions) {
        if (!allowedExtensions.contains(extension)) {
            throw new RuntimeException("不支持的文件类型: " + extension +
                    "。允许的类型: " + String.join(", ", allowedExtensions));
        }
    }

    private static void validateFileSize(MultipartFile file, long maxSize) {
        if (file.getSize() > maxSize) {
            throw new RuntimeException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }
    }

    private static void validateContentType(MultipartFile file, String extension) {
        String contentType = file.getContentType();
        if (contentType == null) {
            log.warn("文件没有ContentType: {}", file.getOriginalFilename());
            return;
        }

        // 简单的MIME类型与扩展名一致性检查
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension) && !contentType.startsWith("image/")) {
            throw new RuntimeException("文件类型与扩展名不匹配");
        }
        if (ALLOWED_VIDEO_EXTENSIONS.contains(extension) && !contentType.startsWith("video/")) {
            throw new RuntimeException("文件类型与扩展名不匹配");
        }
    }

    /**
     * 验证路径安全性（防止路径遍历攻击）
     */
    public static void validatePathSafety(String path) {
        if (path == null) {
            throw new RuntimeException("路径不能为空");
        }

        // 检查路径遍历尝试
        if (path.contains("..") || path.contains("./") || path.contains("/.")) {
            throw new RuntimeException("非法的文件路径");
        }

        // 检查绝对路径
        if (path.startsWith("/") || path.contains(":")) {
            throw new RuntimeException("不允许使用绝对路径");
        }
    }
}
