package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SkuMapper extends BaseMapper<Sku> {
    @Update("update tb_sku set stock - #{num} where id = #{id}")
    int minusStock(@Param("id") Long skuId,@Param("num") Integer num);
}
