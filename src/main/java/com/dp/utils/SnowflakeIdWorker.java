package com.dp.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法ID生成器
 * Twitter的Snowflake算法生成64位的ID
 * 结构如下(每部分用-分开):
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 -
 * 000000000000
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号
 */
@Component
@Slf4j
public class SnowflakeIdWorker {
    // 开始时间截 (2023-01-01)
    private final long twepoch = 1672531200000L;

    // 机器id所占的位数
    private final long workerIdBits = 5L;
    // 数据标识id所占的位数
    private final long datacenterIdBits = 5L;

    // 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);
    // 支持的最大数据标识id，结果是31
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    // 序列在id中占的位数
    private final long sequenceBits = 12L;

    // 机器ID向左移12位
    private final long workerIdShift = sequenceBits;
    // 数据标识id向左移17位(12+5)
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间截向左移22位(5+5+12)
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    // 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 工作机器ID(0~31)
    private long workerId;
    // 数据中心ID(0~31)
    private long datacenterId;
    // 毫秒内序列(0~4095)
    private long sequence = 0L;
    // 上次生成ID的时间截
    private long lastTimestamp = -1L;

    /**
     * 默认构造函数，自动生成workerId和datacenterId
     */
    public SnowflakeIdWorker() {
        this.datacenterId = getDatacenterId();
        this.workerId = getWorkerId();
        log.info("SnowflakeIdWorker初始化 - workerId: {}, datacenterId: {}", workerId, datacenterId);
    }

    /**
     * 构造函数
     * 
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        log.info("SnowflakeIdWorker初始化(手动指定) - workerId: {}, datacenterId: {}", workerId, datacenterId);
    }

    /**
     * 根据IP地址最后一段自动生成workerId
     */
    protected long getWorkerId() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            String hostAddress = address.getHostAddress();

            // 尝试从IP地址获取
            String[] ipParts = hostAddress.split("\\.");
            if (ipParts.length == 4) {
                int lastSegment = Integer.parseInt(ipParts[3]);
                return lastSegment % (maxWorkerId + 1);
            }

            // 如果IP解析失败，使用随机数
            return (int) (Math.random() * (maxWorkerId + 1));
        } catch (UnknownHostException e) {
            log.warn("获取workerId异常，使用随机值: {}", e.getMessage());
            return (int) (Math.random() * (maxWorkerId + 1));
        }
    }

    /**
     * 根据MAC地址生成datacenterId
     */
    protected long getDatacenterId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            if (network == null) {
                return 1;
            }

            byte[] mac = network.getHardwareAddress();
            if (mac == null || mac.length == 0) {
                return 1;
            }

            // 取MAC地址的后几位
            long id = ((0x000000FF & (long) mac[mac.length - 1]) |
                    (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
            return id % (maxDatacenterId + 1);
        } catch (Exception e) {
            log.warn("获取datacenterId异常，使用默认值: {}", e.getMessage());
            return 1;
        }
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     * 
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                            lastTimestamp - timestamp));
        }

        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        // 上次生成ID的时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * 
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     * 
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前workerId
     */
    public long getWorkerIdValue() {
        return workerId;
    }

    /**
     * 获取当前datacenterId
     */
    public long getDatacenterIdValue() {
        return datacenterId;
    }
}