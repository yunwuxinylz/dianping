package com.dp.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dp.dto.Result;
import com.dp.entity.Address;
import com.dp.service.IAddressService;

/**
 * 地址控制器
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private IAddressService addressService;

    /**
     * 获取用户地址列表
     */
    @GetMapping("/list")
    public Result list() {
        return addressService.getUserAddresses();
    }

    /**
     * 添加新地址
     */
    @PostMapping
    public Result add(@RequestBody Address address) {
        return addressService.addAddress(address);
    }

    /**
     * 更新地址
     */
    @PutMapping
    public Result update(@RequestBody Address address) {
        return addressService.updateAddress(address);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long addressId) {
        return addressService.deleteAddress(addressId);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/set-default/{id}")
    public Result setDefault(@PathVariable("id") Long addressId) {
        return addressService.setDefaultAddress(addressId);
    }
}