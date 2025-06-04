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

        // 设置初始状态为处理中
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

    // @Override
    // public Result getUserAfterSales(Integer current, Integer size, Integer
    // status) {
    // // 获取当前用户
    // UserDTO user = UserHolder.getUser();
    // Long userId = user.getId();

    // // 构建查询条件
    // Page<AfterSale> page = this.lambdaQuery()
    // .eq(AfterSale::getUserId, userId)
    // .eq(status != null, AfterSale::getStatus, status)
    // .orderByDesc(AfterSale::getCreateTime)
    // .page(new Page<>(current, size));

    // // 转换为DTO
    // List<AfterSaleDTO> records = page.getRecords().stream()
    // .map(afterSale -> {
    // AfterSaleDTO dto = BeanUtil.copyProperties(afterSale, AfterSaleDTO.class);
    // dto.setOrderId(afterSale.getOrderId().toString());
    // // 处理图片
    // if (StrUtil.isNotBlank(afterSale.getImages())) {
    // List<String> imageList = Arrays.asList(afterSale.getImages().split(","));
    // dto.setImages(imageList);
    // }
    // return dto;
    // })
    // .collect(Collectors.toList());

    // return Result.ok(records, page.getTotal());
    // }

    // TODO: 有问题待解决

    @Override
    @Transactional
    public Result handleAfterSale(AfterSaleStatusDTO afterSaleStatusDTO) {
        // 验证管理员权限
        UserDTO user = UserHolder.getUser();
        boolean isAdmin = user.getIsAdmin();
        if (!isAdmin) {
            return Result.fail("无权操作");
        }

        try {
            // 获取售后记录
            Long id = afterSaleStatusDTO.getId();
            Long orderId = Long.parseLong(afterSaleStatusDTO.getOrderId());
            Long amount = afterSaleStatusDTO.getAmount();

            // 开始事务处理
            Integer type = afterSaleStatusDTO.getType();
            boolean refundResult = true;

            if (afterSaleStatusDTO.getStatus() == 2) { // 只有同意才处理退款
                if (type == 1 || type == 4) {
                    // TODO: 退款
                    refundResult = payService.refund(orderId, amount);
                    if (!refundResult) {
                        return Result.fail("退款处理失败，请稍后重试");
                    }
                }
            }

            // 更新售后状态
            Integer status = afterSaleStatusDTO.getStatus();
            boolean success1 = this.update()
                    .set("status", status)
                    .set("handle_msg", afterSaleStatusDTO.getHandleMsg())
                    .set("handle_time", LocalDateTime.now())
                    .set("update_time", LocalDateTime.now())
                    .eq("id", id)
                    .eq("status", 1)
                    .update();
            if (!success1) {
                return Result.fail("更新售后状态失败");
            }

            // 更新订单售后状态
            boolean success;
            if (status == 2 && (type == 1 || type == 4)) {
                // 同意退款
                success = orderService.update()
                        .set("after_sale_status", status)
                        .setSql("amount = amount - " + amount)
                        .eq("id", orderId)
                        .eq("after_sale_status", 1)
                        .ge("amount", amount)
                        .update();
            } else {
                // 拒绝退款或其他类型的售后
                success = orderService.update()
                        .set("after_sale_status", status)
                        .eq("id", orderId)
                        .eq("after_sale_status", 1)
                        .update();
            }

            if (!success) {
                return Result.fail("更新订单售后状态失败");
            }

            return Result.ok("售后处理成功");
        } catch (Exception e) {
            log.error("售后处理失败", e);
            return Result.fail("售后处理失败: " + e.getMessage());
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