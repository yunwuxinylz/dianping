package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.Result;
import com.dp.entity.Address;

public interface IAddressService extends IService<Address> {
    
    /**
     * 获取用户的地址列表
     */
    Result getUserAddresses();
    
    /**
     * 添加收货地址
     */
    Result addAddress(Address address);
    
    /**
     * 更新收货地址
     */
    Result updateAddress(Address address);
    
    /**
     * 删除收货地址
     */
    Result deleteAddress(Integer addressId);
    
    /**
     * 设置默认收货地址
     */
    Result setDefaultAddress(Integer addressId);
    
    /**
     * 获取默认收货地址
     */
    Result getDefaultAddress();
}