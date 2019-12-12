package com.leyou.item.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    int insertCategoryBrand(@Param("bid") Long id, @Param("ids") List<Long> ids);

    @Delete("delete FROM tb_category_brand where brand_id = #{bid}")
    int deleteBYBrandId(@Param("bid") Long id);

    @Select("SELECT b.id, b.name, b.letter, b.image FROM tb_category_brand cb,tb_brand b WHERE cb.brand_id = b.id AND cb.category_id = #{cid}")
    List<Brand> selectByGroupId(@Param("cid") Long id);
}