package com.dp.controller;

import com.dp.dto.Result;
import com.dp.entity.Address;
import com.dp.service.IAddressService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
     * 获取默认地址
     */
    @GetMapping("/default")
    public Result getDefaultAddress() {
        return addressService.getDefaultAddress();
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
    public Result delete(@PathVariable("id") Integer addressId) {
        return addressService.deleteAddress(addressId);
    }
    
    /**
     * 设置默认地址
     */
    @PutMapping("/{id}/default")
    public Result setDefault(@PathVariable("id") Integer addressId) {
        return addressService.setDefaultAddress(addressId);
    }
}