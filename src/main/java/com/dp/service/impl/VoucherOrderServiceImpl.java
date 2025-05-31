package com.dp.service.impl;

import static com.dp.config.RabbitMQConfig.SECKILL_EXCHANGE;
import static com.dp.config.RabbitMQConfig.SECKILL_ROUTING_KEY;

import java.util.Collections;

import javax.annotation.Resource;

import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import com.dp.utils.SnowflakeIdWorker;
import com.dp.utils.UserHolder;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * 
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
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
        long orderId = snowflakeIdWorker.nextId();
        voucherOrder.setId(orderId);
        // 5.1.6.2.用户id
        voucherOrder.setUserId(userId);
        // 5.1.6.3.代金券id
        voucherOrder.setVoucherId(voucherId);

        rabbitTemplate.convertAndSend(SECKILL_EXCHANGE, SECKILL_ROUTING_KEY, JSONUtil.toJsonStr(voucherOrder));

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
    @Override
    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {

        // 5.1.5.扣减库存
        seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0).update();

        save(voucherOrder);

    }

}
