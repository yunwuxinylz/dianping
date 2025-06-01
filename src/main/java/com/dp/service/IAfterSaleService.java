package com.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dp.dto.AfterSaleDTO;
import com.dp.dto.AfterSaleStatusDTO;
import com.dp.dto.Result;
import com.dp.entity.AfterSale;

public interface IAfterSaleService extends IService<AfterSale> {
    Result submitAfterSale(AfterSaleDTO afterSaleDTO);

    Result queryAfterSalePage(Integer current, Integer size, Integer status);

    Result handleAfterSale(Long id, Integer status, String handleMsg);

    Result getAfterSaleDetail(Long id);

    Result updateAfterSaleStatus(AfterSaleStatusDTO statusDTO);
}