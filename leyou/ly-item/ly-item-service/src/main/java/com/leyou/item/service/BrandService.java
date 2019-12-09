package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;

import java.util.List;

public interface BrandService {
    PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc);

    void saveBrand(BrandDTO brandDTO, List<Long> ids);
}
