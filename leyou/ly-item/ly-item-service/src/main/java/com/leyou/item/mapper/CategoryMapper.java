package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper extends BaseMapper<Category> {
    @Select("SELECT tc.id, tc.`name`, tc.parent_id, tc.is_parent, tc.sort  from \n" +
            "\t\ttb_category_brand tcb,tb_category tc where tcb.category_id = tc.id \n" +
            "\t\tand tcb.brand_id = #{id}")
    List<Category> queryByBrandId(@Param("id") Long bid);
}
