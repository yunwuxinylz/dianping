package com.dp.dto;

import lombok.Data;

/**
 * 地址数据传输对象
 */
@Data
public class AddressDTO {
    /**
     * 地址ID
     */
    private Long id;

    /**
     * 收件人姓名
     */
    private String name;

    /**
     * 收件人电话
     */
    private String phone;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 是否默认地址
     */
    private Boolean isDefault;
}