package com.dp;

import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.dp.utils.RedisIdWorker;

import cn.hutool.core.lang.Assert;

@SpringBootTest // 显式指定主应用类
class DianPingApplicationTests {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("test");
                System.out.println("线程：" + Thread.currentThread().getName() + " 生成的ID: " + id);
                Assert.isTrue(id > 0, "ID必须大于0");
            }
            latch.countDown();
        };
        
        // 创建300个线程同时执行
        for (int i = 0; i < 300; i++) {
            new Thread(task).start();
        }
        
        // 等待所有线程执行完毕
        latch.await();
    }

    @Test
    void simpleTest() {
        System.out.println("这是一个简单的测试");
    }
}
