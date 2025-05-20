package com.dp.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dp.config.OSSConfig;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OSSClient {

    private final OSSConfig ossConfig;

    public OSSClient(OSSConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    /**
     * 上传文件到OSS
     * 
     * @param file 文件
     * @param type 文件类型
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String type) {
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());

        try {
            // 生成OSS存储路径
            String fileName = generateFileName(file.getOriginalFilename(), type);

            // 上传文件
            ossClient.putObject(
                    ossConfig.getBucketName(),
                    fileName,
                    new ByteArrayInputStream(file.getBytes()));

            // 返回可访问路径
            return fileName;
        } catch (IOException e) {
            log.error("文件上传OSS失败", e);
            throw new RuntimeException("文件上传失败", e);
        } finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }
    }

    /**
     * 从OSS删除文件
     * 
     * @param fileName 文件名
     */
    public void deleteFile(String fileName) {
        // 如果是完整URL，提取对象名
        if (fileName.startsWith("http")) {
            // 移除URL前缀部分
            String urlPrefix = ossConfig.getUrlPrefix();
            if (fileName.startsWith(urlPrefix)) {
                fileName = fileName.substring(urlPrefix.length() + 1); // +1 是为了去掉开头的斜杠
            } else {
                // 无法从URL中提取对象名
                log.error("无法删除不属于当前存储桶的文件: {}", fileName);
                return;
            }
        }

        // 创建OSSClient实例并删除文件
        OSS ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret());

        try {
            ossClient.deleteObject(ossConfig.getBucketName(), fileName);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成文件名
     * 
     * @param originalFilename 原始文件名
     * @param type             文件类型
     * @return OSS中的文件路径
     */
    private String generateFileName(String originalFilename, String type) {
        // 获取文件后缀
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        if (StrUtil.isBlank(suffix)) {
            suffix = "jpg";
        }

        // 按日期和类型生成路径
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String randomName = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 最终OSS路径
        return String.format("imgs/%s/%s/%s.%s", type, dateDir, randomName, suffix);
    }

    /**
     * 获取文件访问URL
     * 
     * @param objectName OSS中的对象名
     * @return 完整访问URL
     */
    public String getFileUrl(String objectName) {
        if (objectName.startsWith("http")) {
            return objectName;
        }
        return ossConfig.getUrlPrefix() + "/" + objectName;
    }
}
