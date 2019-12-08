package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public List<CategoryDTO> queryByParentId(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        //直接封装对象查询
        QueryWrapper<Category> wrapper = new QueryWrapper<>(category);
        List<Category> categoryList = categoryMapper.selectList(wrapper);
        //如果没查询出结果，抛出异常
        if (CollectionUtils.isEmpty(categoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //利用BeanHelper工具类转换成CategoryDTO类型
        return BeanHelper.copyWithCollection(categoryList, CategoryDTO.class);
    }
}
