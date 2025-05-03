package com.dp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dp.dto.Result;
import com.dp.dto.UserDTO;
import com.dp.entity.Address;
import com.dp.mapper.AddressMapper;
import com.dp.service.IAddressService;
import com.dp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

    @Override
    public Result getUserAddresses() {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 查询用户的所有地址
        List<Address> addresses = lambdaQuery()
                .eq(Address::getUserId, user.getId())
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt)
                .list();
                
        return Result.ok(addresses);
    }

    @Override
    @Transactional
    public Result addAddress(Address address) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 设置用户ID
        address.setUserId(user.getId());
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        
        // 如果是默认地址，将该用户其他地址设为非默认
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            baseMapper.clearDefaultByUserId(user.getId());
        }
        
        // 保存地址
        save(address);
        
        return Result.ok(address.getAddressId());
    }

    @Override
    @Transactional
    public Result updateAddress(Address address) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 验证地址是否属于当前用户
        Address existingAddress = getById(address.getAddressId());
        if (existingAddress == null) {
            return Result.fail("地址不存在");
        }
        if (!existingAddress.getUserId().equals(user.getId())) {
            return Result.fail("无权修改此地址");
        }
        
        // 设置更新时间
        address.setUpdatedAt(LocalDateTime.now());
        
        // 如果设置为默认地址，将该用户其他地址设为非默认
        if (Boolean.TRUE.equals(address.getIsDefault()) && !Boolean.TRUE.equals(existingAddress.getIsDefault())) {
            baseMapper.clearDefaultByUserId(user.getId());
        }
        
        // 更新地址
        updateById(address);
        
        return Result.ok();
    }

    @Override
    public Result deleteAddress(Integer addressId) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 验证地址是否属于当前用户
        Address existingAddress = getById(addressId);
        if (existingAddress == null) {
            return Result.fail("地址不存在");
        }
        if (!existingAddress.getUserId().equals(user.getId())) {
            return Result.fail("无权删除此地址");
        }
        
        // 删除地址
        removeById(addressId);
        
        return Result.ok();
    }

    @Override
    @Transactional
    public Result setDefaultAddress(Integer addressId) {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 验证地址是否属于当前用户
        Address existingAddress = getById(addressId);
        if (existingAddress == null) {
            return Result.fail("地址不存在");
        }
        if (!existingAddress.getUserId().equals(user.getId())) {
            return Result.fail("无权操作此地址");
        }
        
        // 将该用户所有地址设置为非默认
        baseMapper.clearDefaultByUserId(user.getId());
        
        // 将当前地址设置为默认
        existingAddress.setIsDefault(true);
        existingAddress.setUpdatedAt(LocalDateTime.now());
        updateById(existingAddress);
        
        return Result.ok();
    }

    @Override
    public Result getDefaultAddress() {
        // 获取当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return Result.fail("用户未登录");
        }
        
        // 查询默认地址
        Address defaultAddress = lambdaQuery()
                .eq(Address::getUserId, user.getId())
                .eq(Address::getIsDefault, true)
                .one();
                
        if (defaultAddress == null) {
            // 如果没有默认地址，返回最新添加的地址
            defaultAddress = lambdaQuery()
                    .eq(Address::getUserId, user.getId())
                    .orderByDesc(Address::getCreatedAt)
                    .last("LIMIT 1")
                    .one();
        }
        
        return Result.ok(defaultAddress);
    }
}