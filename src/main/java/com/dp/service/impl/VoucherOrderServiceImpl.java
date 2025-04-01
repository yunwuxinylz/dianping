package com.dp.service.impl;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.Result;
import com.dp.entity.VoucherOrder;
import com.dp.mapper.VoucherOrderMapper;
import com.dp.service.ISeckillVoucherService;
import com.dp.service.IVoucherOrderService;
import com.dp.utils.RedisIdWorker;
import com.dp.utils.UserHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @since 2021-12-22
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private IVoucherOrderService proxy;
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    // 在构造方法或@PostConstruct方法中启动异步线程
    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    // 1.获取队列中的订单信息
                    VoucherOrder voucherOrder = orderTasks.take();
                    // 2.创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();

        // 创建锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        boolean isLock = lock.tryLock();
        if (!isLock) {
            log.error("不允许重复下单");
            return;
        }
        try {
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result seckillVoucher(Long voucherId) {

        Long userId = UserHolder.getUser().getId();

        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString());

        int intValue = result.intValue();
        if (intValue != 0) {
            return Result.fail(intValue == 1 ? "库存不足" : "不能重复下单");
        }

        // 5.1.6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 5.1.6.1.订单id
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        // 5.1.6.2.用户id
        voucherOrder.setUserId(userId);
        // 5.1.6.3.代金券id
        voucherOrder.setVoucherId(voucherId);

        orderTasks.add(voucherOrder);

        // 获取代理对象
        proxy = (IVoucherOrderService) AopContext.currentProxy();

        return Result.ok(orderId);

    }

    // private Result getIsLock(Long voucherId) {
    // // 5.1.1.查询优惠券
    // SeckillVoucher voucher = seckillVoucherService.getById(voucherId);

    // // 5.1.2.判断秒杀是否开始
    // if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
    // // 尚未开始
    // return Result.fail("秒杀尚未开始");

    // }

    // // 5.1.3.判断秒杀是否已经结束
    // if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
    // // 已经结束
    // return Result.fail("秒杀已经结束");

    // }

    // // 5.1.4.判断库存是否充足
    // if (voucher.getStock() < 1) {
    // // 库存不足
    // return Result.fail("库存不足");

    // }
    // Long userId = UserHolder.getUser().getId();

    // // ILock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
    // RLock lock = redissonClient.getLock("lock:order:" + userId);
    // boolean isLock = lock.tryLock();
    // if (!isLock) {
    // return Result.fail("不允许重复下单");
    // }

    // // 获取代理对象（事务）
    // try {
    // IVoucherOrderService proxy = (IVoucherOrderService)
    // AopContext.currentProxy();
    // return proxy.createVoucherOrder(voucherId);
    // } finally {
    // lock.unlock();
    // }
    // }

    /**
     *
     * @param voucherId
     * @return
     */
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 一人一单
        Long userId = voucherOrder.getUserId();
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherOrder.getVoucherId()).count();
        if (count > 0) {
            // 用户已经购买过一次
            log.error("用户已经买过了");
            return;
        }

        // 5.1.5.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId()).gt("stock", 0).update();

        if (!success) {
            // 扣减失败
            log.error("库存不足");
            return;
        }

        save(voucherOrder);

    }

}
