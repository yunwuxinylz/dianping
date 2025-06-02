package com.dp.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.AfterSaleDTO;
import com.dp.dto.AfterSaleStatusDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.AfterSale;
import com.dp.entity.Order;
import com.dp.mapper.AfterSaleMapper;
import com.dp.service.IAfterSaleService;
import com.dp.service.IOrderService;
import com.dp.service.IPayService;
import com.dp.utils.UserHolder;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 售后服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements IAfterSaleService {

    private final IOrderService orderService;
    private final IPayService payService;

    @Override
    @Transactional
    public Result applyAfterSale(AfterSaleDTO afterSaleDTO) {
        // 获取当前用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 获取订单信息
        Long orderId = Long.parseLong(afterSaleDTO.getOrderId());
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId)) {
            return Result.fail("无权操作此订单");
        }

        // 校验订单状态
        if (order.getStatus() <= 1) {
            return Result.fail("订单未支付，不能申请售后");
        }

        // 校验订单是否已有售后申请
        Long count = this.lambdaQuery()
                .eq(AfterSale::getOrderId, orderId)
                .eq(AfterSale::getStatus, 1)
                .count();
        if (count > 0) {
            return Result.fail("该订单已有售后申请正在处理中，请勿重复申请");
        }

        // 创建售后记录
        AfterSale afterSale = new AfterSale();
        afterSale.setOrderId(orderId);
        afterSale.setUserId(userId);
        afterSale.setShopId(order.getShopId());
        afterSale.setType(afterSaleDTO.getType());
        afterSale.setReason(afterSaleDTO.getReason());
        afterSale.setAmount(afterSaleDTO.getAmount());
        afterSale.setDescription(afterSaleDTO.getDescription());

        // 处理图片
        if (afterSaleDTO.getImages() != null && !afterSaleDTO.getImages().isEmpty()) {
            String images = String.join(",", afterSaleDTO.getImages());
            afterSale.setImages(images);
        }

        // 设置初始状态为待处理
        afterSale.setStatus(1);
        afterSale.setCreateTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());

        // 保存售后记录
        this.save(afterSale);

        // 更新订单售后状态
        orderService.update()
                .set("after_sale_status", afterSale.getStatus())
                .eq("id", orderId)
                .update();

        return Result.ok(afterSale.getId());
    }

    @Override
    public Result getAfterSaleDetail(Long id) {
        // 获取当前用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
        boolean isAdmin = user.getIsAdmin();

        // 查询售后记录
        AfterSale afterSale = this.getById(id);
        if (afterSale == null) {
            return Result.fail("售后记录不存在");
        }

        // 非管理员只能查看自己的售后记录
        if (!isAdmin && !afterSale.getUserId().equals(userId)) {
            return Result.fail("无权查看此售后记录");
        }

        // 转换为DTO
        AfterSaleDTO afterSaleDTO = BeanUtil.copyProperties(afterSale, AfterSaleDTO.class);
        afterSaleDTO.setOrderId(afterSale.getOrderId().toString());

        // 处理图片
        if (StrUtil.isNotBlank(afterSale.getImages())) {
            List<String> imageList = Arrays.asList(afterSale.getImages().split(","));
            afterSaleDTO.setImages(imageList);
        }

        return Result.ok(afterSaleDTO);
    }

    @Override
    public Result getAfterSaleByOrderId(Long orderId) {
        // 获取当前用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 查询订单
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        // 校验订单归属
        if (!order.getUserId().equals(userId) && !user.getIsAdmin()) {
            return Result.fail("无权查看此订单");
        }

        // 查询售后记录
        List<AfterSale> afterSales = this.lambdaQuery()
                .eq(AfterSale::getOrderId, orderId)
                .list();

        // 转换为DTO
        List<AfterSaleDTO> afterSaleDTOs = afterSales.stream()
                .map(afterSale -> {
                    AfterSaleDTO dto = BeanUtil.copyProperties(afterSale, AfterSaleDTO.class);
                    dto.setOrderId(afterSale.getOrderId().toString());
                    // 处理图片
                    if (StrUtil.isNotBlank(afterSale.getImages())) {
                        List<String> imageList = Arrays.asList(afterSale.getImages().split(","));
                        dto.setImages(imageList);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return Result.ok(afterSaleDTOs);
    }

    @Override
    public Result getUserAfterSales(Integer current, Integer size, Integer status) {
        // 获取当前用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();

        // 构建查询条件
        Page<AfterSale> page = this.lambdaQuery()
                .eq(AfterSale::getUserId, userId)
                .eq(status != null, AfterSale::getStatus, status)
                .orderByDesc(AfterSale::getCreateTime)
                .page(new Page<>(current, size));

        // 转换为DTO
        List<AfterSaleDTO> records = page.getRecords().stream()
                .map(afterSale -> {
                    AfterSaleDTO dto = BeanUtil.copyProperties(afterSale, AfterSaleDTO.class);
                    dto.setOrderId(afterSale.getOrderId().toString());
                    // 处理图片
                    if (StrUtil.isNotBlank(afterSale.getImages())) {
                        List<String> imageList = Arrays.asList(afterSale.getImages().split(","));
                        dto.setImages(imageList);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return Result.ok(records, page.getTotal());
    }

    @Override
    @Transactional
    public Result handleAfterSale(AfterSaleStatusDTO afterSaleStatusDTO) {
        // 验证管理员权限
        UserDTO user = UserHolder.getUser();
        boolean isAdmin = user.getIsAdmin();
        if (!isAdmin) {
            return Result.fail("无权操作");
        }

        // 获取售后记录
        Long id = afterSaleStatusDTO.getId();
        AfterSale afterSale = this.getById(id);
        if (afterSale == null) {
            return Result.fail("售后记录不存在");
        }

        // 只能处理待处理状态的售后申请
        if (afterSale.getStatus() != 1) {
            return Result.fail("该售后申请已被处理");
        }

        // 更新售后状态
        Integer status = afterSaleStatusDTO.getStatus();
        afterSale.setStatus(status);
        afterSale.setHandleMsg(afterSaleStatusDTO.getHandleMsg());
        afterSale.setHandleTime(LocalDateTime.now());
        afterSale.setUpdateTime(LocalDateTime.now());
        this.updateById(afterSale);

        // 更新订单售后状态
        orderService.update()
                .set("after_sale_status", status)
                .eq("id", afterSale.getOrderId())
                .update();

        // 如果是同意售后并且是退款，则进行退款操作
        if (status == 1 && afterSale.getType() == 4) {
            return this.refund(id);
        }

        return Result.ok();
    }

    @Override
    @Transactional
    public Result refund(Long id) {
        // 获取售后记录
        AfterSale afterSale = this.getById(id);
        if (afterSale == null) {
            return Result.fail("售后记录不存在");
        }

        // 获取订单信息
        Long orderId = afterSale.getOrderId();
        Order order = orderService.getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }

        try {
            // 调用支付宝退款接口
            boolean refundResult = payService.refund(orderId, afterSale.getAmount());

            if (!refundResult) {
                return Result.fail("退款失败，请稍后再试");
            }

            // 更新售后状态
            afterSale.setStatus(2);
            afterSale.setUpdateTime(LocalDateTime.now());
            this.updateById(afterSale);

            // 更新订单售后状态
            orderService.update()
                    .set("after_sale_status", 2)
                    .eq("id", orderId)
                    .update();

            // 扣除订单金额（部分退款）
            if (afterSale.getAmount() < order.getAmount()) {
                orderService.update()
                        .set("amount", order.getAmount() - afterSale.getAmount())
                        .eq("id", orderId)
                        .update();
            }

            return Result.ok("退款成功");
        } catch (Exception e) {
            log.error("退款失败", e);
            return Result.fail("退款失败：" + e.getMessage());
        }
    }

    @Override
    public Result getAllAfterSales(Integer current, Integer size, Integer status) {
        // 验证管理员权限
        UserDTO user = UserHolder.getUser();
        boolean isAdmin = user.getIsAdmin();
        if (!isAdmin) {
            return Result.fail("无权操作");
        }

        // 构建查询条件
        Page<AfterSale> page = this.lambdaQuery()
                .eq(status != null, AfterSale::getStatus, status)
                .orderByDesc(AfterSale::getCreateTime)
                .page(new Page<>(current, size));

        // 转换为DTO
        List<AfterSaleDTO> records = page.getRecords().stream()
                .map(afterSale -> {
                    AfterSaleDTO dto = BeanUtil.copyProperties(afterSale, AfterSaleDTO.class);
                    // 处理图片
                    if (StrUtil.isNotBlank(afterSale.getImages())) {
                        List<String> imageList = Arrays.asList(afterSale.getImages().split(","));
                        dto.setImages(imageList);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        return Result.ok(records, page.getTotal());
    }
}