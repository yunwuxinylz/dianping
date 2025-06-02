package com.dp.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.GoodsDTO;
import com.dp.dto.GoodsSearchDTO;
import com.dp.dto.Result;
import com.dp.entity.GoodSKU;
import com.dp.entity.Goods;
import com.dp.entity.Shop;
import com.dp.mapper.GoodsMapper;
import com.dp.service.IGoodSKUService;
import com.dp.service.IGoodsService;
import com.dp.service.IShopService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

// 商品Service实现
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    private final IGoodSKUService goodSKUService;

    private final IShopService shopService;

    public GoodsServiceImpl(IGoodSKUService goodSKUService, IShopService shopService) {
        this.goodSKUService = goodSKUService;
        this.shopService = shopService;
    }

    /**
     * 根据店铺ID查询商品列表
     * 
     * @param shopId
     * @return
     */
    @Override
    public List<GoodsDTO> queryGoodsByShopId(Long shopId) {
        // 查询商品列表
        List<Goods> goodsList = query().eq("shop_id", shopId).eq("status", 1).list();
        // 转换为DTO
        return goodsList.stream().map(goods -> {
            GoodsDTO goodsDTO = BeanUtil.copyProperties(goods, GoodsDTO.class);
            if (goods.getImages() != null) {
                goodsDTO.setImages(Arrays.asList(goods.getImages().split(",")));
            }
            return goodsDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 根据商品ID查询商品详情
     * 
     * @param id
     * @return
     */
    @Override
    public GoodsDTO queryGoodsById(Long id) {
        // 查询商品
        Goods goods = getById(id);
        if (goods == null) {
            return null;
        }
        // 转换为DTO
        GoodsDTO goodsDTO = BeanUtil.copyProperties(goods, GoodsDTO.class);
        goodsDTO.setImages(Arrays.asList(goods.getImages().split(",")));

        // 查询商品的SKU列表
        List<GoodSKU> skuList = goodSKUService.query()
                .eq("goods_id", id)
                .list();
        goodsDTO.setSkus(skuList);

        return goodsDTO;
    }

    /**
     * 商品搜索列表
     * 
     * @param name
     * @param sortBy
     * @param sortOrder
     * @param pageSize
     * @param current
     * @return
     */
    @Override
    public Result goodsSearchList(String name, String sortBy, String sortOrder, Integer pageSize, Integer current) {
        Page<Goods> page = this.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .orderBy(StrUtil.isNotBlank(sortBy),
                        "ASC".equalsIgnoreCase(sortOrder),
                        sortBy)
                .page(new Page<>(current, pageSize));

        // 获取所有店铺ID
        List<Long> shopIds = page.getRecords().stream()
                .map(Goods::getShopId)
                .collect(Collectors.toList());

        // 一次性查询所有店铺信息
        Map<Long, Shop> shopMap = shopIds.isEmpty()
                ? new HashMap<>()
                : shopService.listByIds(shopIds).stream()
                        .collect(Collectors.toMap(Shop::getId, shop -> shop));

        // 转换为DTO
        List<GoodsSearchDTO> goodsDTOList = page.getRecords().stream()
                .map(item -> {
                    GoodsSearchDTO goodsDTO = BeanUtil.copyProperties(item, GoodsSearchDTO.class);
                    if (item.getImages() != null) {
                        goodsDTO.setImages(Arrays.asList(item.getImages().split(",")));
                    }
                    // 从Map中获取店铺信息
                    Shop shop = shopMap.get(item.getShopId());
                    if (shop != null) {
                        goodsDTO.setShopName(shop.getName());
                        goodsDTO.setAddress(shop.getAddress());
                        goodsDTO.setScore(shop.getScore());
                        goodsDTO.setDistance(shop.getDistance());

                    }
                    return goodsDTO;
                })
                .collect(Collectors.toList());

        return Result.ok(goodsDTOList, page.getTotal());
    }

    /**
     * 商品推荐列表
     * 
     * @param count
     * @return
     */
    @Override
    public Result goodsRecommendList(Integer count) {
        // 随机获取商品
        List<Goods> goods = this.query()
                .orderByDesc("sold")
                .last("LIMIT " + count)
                .list();

        // 转换为DTO
        List<GoodsSearchDTO> goodsDTOList = goods.stream()
                .map(item -> {
                    GoodsSearchDTO goodsDTO = BeanUtil.copyProperties(item, GoodsSearchDTO.class);
                    if (item.getImages() != null) {
                        goodsDTO.setImages(Arrays.asList(item.getImages().split(",")));
                    }
                    // 根据店铺id查询店铺详情
                    Shop shop = shopService.getById(item.getShopId());
                    if (shop != null) {
                        goodsDTO.setShopName(shop.getName());
                        goodsDTO.setAddress(shop.getAddress());
                    }
                    return goodsDTO;
                })
                .collect(Collectors.toList());

        return Result.ok(goodsDTOList);
    }

    @Override
    public Result getGoodsCount() {
        try {
            // 从数据库获取商品总数
            // 使用 MyBatis-Plus 的 selectCount 方法
            long count = count(); // 或者 goodsMapper.selectCount(null);
            return Result.ok(count); // 使用 Result.ok 而不是 Result.success
        } catch (Exception e) {
            // 记录日志会更好
            // log.error("获取商品总数失败", e);
            return Result.fail("获取商品总数失败"); // 使用 Result.fail 而不是 Result.error
        }
    }

    @Override
    public Result adminGoodsList(String name, Long shopId, Integer status, Integer pageSize, Integer current) {
        // 构建查询条件
        Page<Goods> page = this.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .eq(shopId != null, "shop_id", shopId)
                .eq(status != null, "status", status)
                .page(new Page<>(current, pageSize));

        // 获取所有店铺ID
        List<Long> shopIds = page.getRecords().stream()
                .map(Goods::getShopId)
                .collect(Collectors.toList());

        // 一次性查询所有店铺信息
        Map<Long, Shop> shopMap = shopIds.isEmpty()
                ? new HashMap<>()
                : shopService.listByIds(shopIds).stream()
                        .collect(Collectors.toMap(Shop::getId, shop -> shop));

        // 转换为DTO
        List<GoodsDTO> goodsDTOList = page.getRecords().stream()
                .map(item -> {
                    GoodsDTO goodsDTO = BeanUtil.copyProperties(item, GoodsDTO.class);
                    if (item.getImages() != null) {
                        goodsDTO.setImages(Arrays.asList(item.getImages().split(",")));
                    }
                    // 从Map中获取店铺信息
                    Shop shop = shopMap.get(item.getShopId());
                    if (shop != null) {
                        goodsDTO.setShopName(shop.getName());
                    }
                    return goodsDTO;
                }).collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("list", goodsDTOList);
        return Result.ok(result);
    }

    @Override
    public Result updateGoods(Goods goods) {
        if (goods.getId() == null) {
            return Result.fail("商品id不能为空");
        }
        // 将 imagesList List 转为字符串存储
        if (goods.getImagesList() != null && !goods.getImagesList().isEmpty()) {
            String images = String.join(",", goods.getImagesList());
            goods.setImages(images);
        }
        boolean success = updateById(goods);
        if (!success) {
            return Result.fail("更新失败");
        }
        return Result.ok();
    }

    @Override
    public Result updateGoodsStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return Result.fail("参数不能为空");
        }
        Goods goods = new Goods();
        goods.setId(id);
        goods.setStatus(status);
        boolean success = updateById(goods);
        if (!success) {
            return Result.fail("更新商品状态失败");
        }
        return Result.ok();
    }

    @Override
    public Result deleteGoods(Long id) {
        if (id == null) {
            return Result.fail("商品ID不能为空");
        }

        // 先查询商品是否存在
        Goods goods = getById(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }

        // 执行删除操作
        boolean success = removeById(id);
        if (!success) {
            return Result.fail("删除失败");
        }

        return Result.ok();
    }

    @Override
    public Result addGoods(Goods goods) {
        // 参数校验
        if (goods == null) {
            return Result.fail("商品信息不能为空");
        }
        if (goods.getShopId() == null) {
            return Result.fail("店铺ID不能为空");
        }
        if (StrUtil.isBlank(goods.getName())) {
            return Result.fail("商品名称不能为空");
        }
        if (goods.getPrice() == null) {
            return Result.fail("商品价格不能为空");
        }
        if (goods.getOriginalPrice() == null) {
            goods.setOriginalPrice(goods.getPrice()); // 默认原价等于现价
        }

        // 处理图片列表
        if (goods.getImagesList() != null && !goods.getImagesList().isEmpty()) {
            String images = String.join(",", goods.getImagesList());
            goods.setImages(images);
        }

        // 设置默认值
        goods.setStatus(1); // 默认上架状态
        goods.setSold(0); // 初始销量为0
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());

        // 保存商品
        boolean success = save(goods);
        if (!success) {
            return Result.fail("新增商品失败");
        }

        return Result.ok(goods.getId());
    }
}