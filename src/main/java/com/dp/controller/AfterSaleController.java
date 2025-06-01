package com.dp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dp.dto.AfterSaleDTO;
import com.dp.dto.AfterSaleStatusDTO;
import com.dp.dto.Result;
import com.dp.service.IAfterSaleService;
import com.dp.utils.OSSClient;

@RestController
@RequestMapping("/after-sale")
public class AfterSaleController {

    private final IAfterSaleService afterSaleService;
    private final OSSClient ossClient;

    public AfterSaleController(IAfterSaleService afterSaleService, OSSClient ossClient) {
        this.afterSaleService = afterSaleService;
        this.ossClient = ossClient;
    }

    @PostMapping("/submit")
    public Result submitAfterSale(@RequestBody AfterSaleDTO afterSaleDTO) {
        return afterSaleService.submitAfterSale(afterSaleDTO);
    }

    @PostMapping("/upload")
    public Result uploadAfterSaleImage(@RequestParam("file") MultipartFile file) {
        try {
            // 上传图片到OSS，指定type为after-sale
            String objectName = ossClient.uploadFile(file, "after-sale");
            // 获取可访问的URL
            String fileUrl = ossClient.getFileUrl(objectName);
            return Result.ok(fileUrl);
        } catch (Exception e) {
            return Result.fail("图片上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/page")
    public Result getAfterSalePage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status) {

        return afterSaleService.queryAfterSalePage(current, size, status);
    }

    @PutMapping("/handle")
    public Result handleAfterSale(@RequestBody AfterSaleStatusDTO statusDTO) {
        return afterSaleService.handleAfterSale(
                statusDTO.getId(),
                statusDTO.getStatus(),
                statusDTO.getHandleMsg());
    }

    @GetMapping("/detail/{id}")
    public Result getAfterSaleDetail(@PathVariable Long id) {
        return afterSaleService.getAfterSaleDetail(id);
    }
}