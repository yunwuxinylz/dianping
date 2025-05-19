package com.dp.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dp.dto.Result;
import com.dp.utils.SystemConstants;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("upload")
public class UploadController {

    /**
     * 删除图片
     * 
     * @param filename
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteBlogImg(@RequestParam String filename) {
        File file = new File(SystemConstants.IMAGE_UPLOAD_DIR, filename);
        if (file.isDirectory()) {
            return Result.fail("错误的文件名称");
        }

        // 如果文件存在，则删除
        if (file.exists()) {
            FileUtil.del(file);
        }

        return Result.ok(file);
    }

    /**
     * 上传图片
     * 
     * @param image
     * @param type
     * @return
     */
    @PostMapping("/save")
    public Result uploadFile(@RequestParam("file") MultipartFile image,
            @RequestParam("type") String type) {
        try {
            // 验证文件类型参数
            if (StrUtil.isBlank(type)) {
                return Result.fail("文件类型不能为空");
            }

            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            // 生成新文件名，使用通用方法并传入类型
            String fileName = createNewFileName(originalFilename, type);
            // 保存文件
            image.transferTo(new File(SystemConstants.IMAGE_UPLOAD_DIR, fileName));
            // 返回结果
            log.debug("文件上传成功，类型：{}，文件名：{}", type, fileName);
            return Result.ok(fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 创建新文件名
     * 
     * @param originalFilename
     * @param type
     * @return
     */
    private String createNewFileName(String originalFilename, String type) {
        // 获取后缀，并确保后缀安全有效
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        if (StrUtil.isBlank(suffix)) {
            suffix = "jpg"; // 默认后缀
        }

        // 按类型和日期组织目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成唯一文件名（更短且依然唯一）
        String randomName = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 构建相对路径，不同类型的图片存放在不同的子目录
        String relativePath = String.format("/imgs/%s/%s", type, dateDir);

        // 创建目录
        File dir = new File(SystemConstants.IMAGE_UPLOAD_DIR, relativePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 返回最终文件路径（带相对路径）
        return String.format("%s/%s.%s", relativePath, randomName, suffix);
    }
}
