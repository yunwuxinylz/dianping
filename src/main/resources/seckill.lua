-- 接收优惠券id参数
local voucherId = ARGV[1]

-- 接收用户id参数
local userId = ARGV[2]

-- 构建库存的key
local stockKey = 'seckill:stock:' .. voucherId

-- 构建订单的key
local orderKey = 'seckill:order:' .. voucherId

-- 获取库存数量
local stock = redis.call('get', stockKey)

-- 判断库存是否存在
if not stock then
    return 1
end

-- 判断库存是否充足
if (tonumber(stock) <= 0) then
    -- 库存不足，返回1
    return 1
end

-- 判断用户是否已经下单
local order = redis.call('sismember', orderKey, userId)

-- 如果用户已经下单，返回2
if (order == 1) then
    return 2
end

-- 扣减库存
redis.call('incrby', stockKey, -1)

-- 将用户id存入订单set集合
redis.call('sadd', orderKey, userId)
-- 返回0，代表下单成功
return 0
