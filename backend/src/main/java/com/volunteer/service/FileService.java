package com.volunteer.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 访问URL
     */
    String uploadFile(MultipartFile file);

    /**
     * 上传文件到指定目录
     *
     * @param file   文件
     * @param folder 子目录
     * @return 访问URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
}
