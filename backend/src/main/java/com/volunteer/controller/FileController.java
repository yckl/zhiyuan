package com.volunteer.controller;

import com.volunteer.common.Result;
import com.volunteer.service.AuthService;
import com.volunteer.service.FileService;
import com.volunteer.util.FileUploadValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 * 所有文件操作需要登录
 */
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "文件上传", description = "文件上传管理")
public class FileController {

    private final FileService fileService;
    private final AuthService authService;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传单个文件")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        try {
            // 文件安全验证
            FileUploadValidator.validate(file);

            String url = fileService.uploadFile(file, folder);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", file.getOriginalFilename());
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 多文件上传
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传", description = "批量上传多个文件")
    public Result<List<Map<String, String>>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false) String folder) {
        try {
            // 验证所有文件
            for (MultipartFile file : files) {
                FileUploadValidator.validate(file);
            }

            List<Map<String, String>> results = new ArrayList<>();
            for (MultipartFile file : files) {
                String url = fileService.uploadFile(file, folder);
                Map<String, String> item = new HashMap<>();
                item.put("url", url);
                item.put("name", file.getOriginalFilename());
                results.add(item);
            }
            return Result.success("上传成功", results);
        } catch (Exception e) {
            log.error("批量文件上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传头像 - 同时更新数据库
     */
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像并更新到数据库")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 头像必须是图片
            FileUploadValidator.validateImage(file);

            // 1. 上传文件获取URL
            String url = fileService.uploadFile(file, "avatar");

            // 2. 获取当前登录用户并更新数据库【关键修复】
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            authService.updateUserAvatar(username, url);

            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传活动封面
     */
    @PostMapping("/upload/cover")
    @Operation(summary = "上传封面", description = "上传活动封面图片")
    public Result<Map<String, String>> uploadCover(@RequestParam("file") MultipartFile file) {
        try {
            // 封面必须是图片
            FileUploadValidator.validateImage(file);

            String url = fileService.uploadFile(file, "cover");
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("封面上传失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "删除已上传的文件")
    public Result<Void> deleteFile(@RequestParam String url) {
        try {
            fileService.deleteFile(url);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
