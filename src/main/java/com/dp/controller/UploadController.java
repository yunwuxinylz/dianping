package com.dp.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dp.dto.Result;
import com.dp.utils.OSSClient;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    private final OSSClient ossClient;

    public UploadController(OSSClient ossClient) {
        this.ossClient = ossClient;
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/delete")
    public Result deleteBlogImg(@RequestParam String filename) {
        if (StrUtil.isBlank(filename)) {
            return Result.fail("文件名不能为空");
        }

        try {
            ossClient.deleteFile(filename);
            return Result.ok();
        } catch (Exception e) {
            log.error("删除OSS文件失败", e);
            return Result.fail("删除文件失败");
        }
    }

    /**
     * 上传图片
     */
    @PostMapping("/save")
    public Result uploadFile(@RequestParam("file") MultipartFile image,
            @RequestParam("type") String type) {
        try {
            if (StrUtil.isBlank(type)) {
                return Result.fail("文件类型不能为空");
            }

            // 上传到OSS，得到对象名
            String objectName = ossClient.uploadFile(image, type);

            // 获取完整的访问URL（带签名）
            String fileUrl = ossClient.getFileUrl(objectName);

            log.debug("文件上传成功，类型：{}，OSS路径：{}", type, fileUrl);
            return Result.ok(fileUrl); // 返回完整URL而非仅对象名
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败：" + e.getMessage());
        }
    }
}
