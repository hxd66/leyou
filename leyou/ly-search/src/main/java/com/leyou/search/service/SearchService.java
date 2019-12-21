package com.leyou.search.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;

import java.util.List;
import java.util.Map;

public interface SearchService {
    //把一个Spu转为Goods对象
    Goods buildGoods(SpuDTO spuDTO);

    PageResult<GoodsDTO> search(SearchRequest searchRequest);

    //查询过滤项
    Map<String, List> queryFilters(SearchRequest searchRequest);

    void createIndex(Long id);

    void deleteIndex(Long id);
}
