package com.leyou.item.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    int insertCategoryBrand(@Param("bid") Long id, @Param("ids") List<Long> ids);

    @Delete("delete FROM tb_category_brand where brand_id = #{bid}")
    int deleteBYBrandId(@Param("bid") Long id);
}