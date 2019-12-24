package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;

import java.util.List;

public interface GoodsService {
    PageResult<SpuDTO> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows);

    void saveSpu(SpuDTO spuDTO);

    void updateSaleable(Long id, Boolean saleable);

    SpuDetailDTO querySpuDetailById(Long id);

    List<SkuDTO> querySkuBySpuId(Long id);

    void updateGoods(SpuDTO spuDTO);

    void deleteGoods(Long id);

    SpuDTO querySpuById(Long id);

    List<SkuDTO> querySkusByIds(List<Long> ids);
}
