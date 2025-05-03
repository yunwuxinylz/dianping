package com.dp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dp.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    
    /**
     * 将用户所有地址设置为非默认
     */
    @Update("UPDATE tb_address SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefaultByUserId(@Param("userId") Long userId);
}