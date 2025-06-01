package com.dp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.AfterSaleDTO;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.dto.AfterSaleStatusDTO;
import com.dp.entity.AfterSale;
import com.dp.entity.Order;
import com.dp.mapper.AfterSaleMapper;
import com.dp.service.IAfterSaleService;
import com.dp.service.IOrderService;
import com.dp.utils.UserHolder;
import com.dp.utils.OSSClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements IAfterSaleService {

    private final IOrderService orderService;
    private final OSSClient ossClient;

    public AfterSaleServiceImpl(IOrderService orderService, OSSClient ossClient) {
        this.orderService = orderService;
        this.ossClient = ossClient;
    }

    @Override
    @Transactional
    public Result submitAfterSale(AfterSaleDTO afterSaleDTO) {
        // 1. 参数校验
        if (afterSaleDTO.getOrderId() == null || afterSaleDTO.getType() == null
                || afterSaleDTO.getAmount() == null) {
            return Result.fail("参数不完整");
        }

        // 2. 获取当前用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        Long userId = user.getId(); // 从UserDTO中获取用户ID

        try {
            // 3. 查询订单信息
            Order order = orderService.getById(afterSaleDTO.getOrderId());
            if (order == null) {
                return Result.fail("订单不存在");
            }

            // 4. 校验订单是否属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.fail("无权操作此订单");
            }

            // 5. 校验订单状态（只有已完成的订单可以申请售后）
            if (order.getStatus() != 5) {
                return Result.fail("订单状态不符合申请售后条件");
            }

            // 6. 校验退款金额
            if (afterSaleDTO.getAmount() > order.getAmount()) {
                return Result.fail("退款金额不能超过订单金额");
            }

            // 7. 创建售后记录
            AfterSale afterSale = new AfterSale();
            afterSale.setOrderId(afterSaleDTO.getOrderId());
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

            // 8. 保存售后记录
            boolean success = save(afterSale);
            if (!success) {
                return Result.fail("申请售后失败");
            }

            return Result.ok(afterSale.getId());
        } catch (Exception e) {
            log.error("提交售后申请失败", e);
            return Result.fail("服务器异常");
        }
    }

    public void deleteAfterSaleImages(String images) {
        if (images != null && !images.isEmpty()) {
            String[] imageUrls = images.split(",");
            for (String imageUrl : imageUrls) {
                try {
                    ossClient.deleteFile(imageUrl);
                } catch (Exception e) {
                    log.error("删除售后图片失败: {}", imageUrl, e);
                }
            }
        }
    }

    @Override
    public Result queryAfterSalePage(Integer current, Integer size, Integer status) {
        // 1.创建分页对象
        Page<AfterSale> page = new Page<>(current, size);

        // 2.创建查询条件
        LambdaQueryWrapper<AfterSale> queryWrapper = new LambdaQueryWrapper<>();

        // 3.添加状态筛选条件
        if (status != null) {
            queryWrapper.eq(AfterSale::getStatus, status);
        }

        // 4.添加排序条件（按创建时间倒序）
        queryWrapper.orderByDesc(AfterSale::getCreateTime);

        // 5.执行查询
        page = page(page, queryWrapper);

        // 6.获取分页数据
        List<AfterSale> records = page.getRecords();

        // 7.判空
        if (records == null || records.isEmpty()) {
            return Result.ok(page);
        }

        // 8.查询关联的订单和用户信息
        List<Map<String, Object>> afterSaleVOList = records.stream().map(afterSale -> {
            Map<String, Object> map = new HashMap<>();
            // 复制售后信息
            map.put("id", afterSale.getId());
            map.put("orderId", afterSale.getOrderId());
            map.put("userId", afterSale.getUserId());
            map.put("type", afterSale.getType());
            map.put("reason", afterSale.getReason());
            map.put("amount", afterSale.getAmount());
            map.put("description", afterSale.getDescription());
            map.put("images", afterSale.getImages());
            map.put("status", afterSale.getStatus());
            map.put("handleMsg", afterSale.getHandleMsg());
            map.put("handleTime", afterSale.getHandleTime());
            map.put("createTime", afterSale.getCreateTime());

            // 查询关联的订单信息
            Order order = orderService.getById(afterSale.getOrderId());
            if (order != null) {
                map.put("orderAmount", order.getAmount());
                map.put("orderStatus", order.getStatus());
            }

            return map;
        }).collect(Collectors.toList());

        // 9.封装返回结果
        Page<Map<String, Object>> resultPage = new Page<>(current, size, page.getTotal());
        resultPage.setRecords(afterSaleVOList);

        return Result.ok(resultPage);
    }

    @Override
    @Transactional
    public Result handleAfterSale(Long id, Integer status, String handleMsg) {
        // 1.查询售后记录
        AfterSale afterSale = getById(id);
        if (afterSale == null) {
            return Result.fail("售后记录不存在");
        }

        // 2.判断状态是否可以处理
        if (afterSale.getStatus() != 1) {
            return Result.fail("该售后申请已处理");
        }

        // 3.更新售后状态
        afterSale.setStatus(status);
        afterSale.setHandleMsg(handleMsg);
        afterSale.setHandleTime(LocalDateTime.now());

        // 4.保存更新
        boolean success = updateById(afterSale);
        if (!success) {
            return Result.fail("处理失败");
        }

        return Result.ok();
    }

    @Override
    public Result getAfterSaleDetail(Long id) {
        // 1.查询售后记录
        AfterSale afterSale = getById(id);
        if (afterSale == null) {
            return Result.fail("售后记录不存在");
        }

        // 2.查询关联的订单信息
        Order order = orderService.getById(afterSale.getOrderId());

        // 3.构建返回数据
        Map<String, Object> map = new HashMap<>();
        // 售后信息
        map.put("id", afterSale.getId());
        map.put("orderId", afterSale.getOrderId());
        map.put("userId", afterSale.getUserId());
        map.put("type", afterSale.getType());
        map.put("reason", afterSale.getReason());
        map.put("amount", afterSale.getAmount());
        map.put("description", afterSale.getDescription());
        map.put("images", afterSale.getImages());
        map.put("status", afterSale.getStatus());
        map.put("handleMsg", afterSale.getHandleMsg());
        map.put("handleTime", afterSale.getHandleTime());
        map.put("createTime", afterSale.getCreateTime());

        // 订单信息
        if (order != null) {
            map.put("orderAmount", order.getAmount());
            map.put("orderStatus", order.getStatus());
            map.put("shopName", order.getShopName());
            map.put("addressName", order.getAddressName());
            map.put("addressPhone", order.getAddressPhone());
            map.put("addressDetail", order.getAddressDetail());
        }

        return Result.ok(map);
    }

    @Override
    @Transactional
    public Result updateAfterSaleStatus(AfterSaleStatusDTO statusDTO) {
        // 1.参数校验
        if (statusDTO.getId() == null || statusDTO.getStatus() == null) {
            return Result.fail("参数不完整");
        }

        // 2.查询售后记录
        AfterSale afterSale = getById(statusDTO.getId());
        if (afterSale == null) {
            return Result.fail("售后记录不存在");
        }

        // 3.判断状态是否可以更新
        if (afterSale.getStatus() != 1) {
            return Result.fail("该售后申请已处理");
        }

        // 4.更新售后状态
        afterSale.setStatus(statusDTO.getStatus());
        afterSale.setHandleMsg(statusDTO.getHandleMsg());
        afterSale.setHandleTime(LocalDateTime.now());

        // 5.保存更新
        boolean success = updateById(afterSale);
        if (!success) {
            return Result.fail("更新状态失败");
        }

        // 6.如果是同意售后(status=3)，更新订单状态为"售后中"(6)
        if (statusDTO.getStatus() == 3) {
            Result result = orderService.updateOrderStatus(afterSale.getOrderId(), 6);
            if (!result.getSuccess()) {
                throw new RuntimeException("更新订单状态失败");
            }
        }

        return Result.ok();
    }
}