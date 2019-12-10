package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;

public interface GoodsService {
    PageResult<SpuDTO> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows);
}
