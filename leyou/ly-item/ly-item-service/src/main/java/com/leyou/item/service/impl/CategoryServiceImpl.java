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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
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

    @Override
    public List<CategoryDTO> queryByBrandId(Long bid) {
        List<Category> list = categoryMapper.queryByBrandId(bid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list,CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> queryCategoryByIds(List<Long> ids) {
        List<Category> categoryList = categoryMapper.selectBatchIds(ids);
        return BeanHelper.copyWithCollection(categoryList,CategoryDTO.class);
    }

    @Override
    public List<CategoryDTO> queryAllByCid3(Long id) {
        //先根据3级分类查询对应的分类
        Category category3 = categoryMapper.selectById(id);
        if (category3 == null){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //再根据3级分类的父id查询分类
        Category category2 = categoryMapper.selectById(category3.getParentId());
        if (category2 == null){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //再根据2级分类的父id查询分类
        Category category1 = categoryMapper.selectById(category2.getParentId());
        if (category1 == null){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //转换为集合
        List<Category> list = Arrays.asList(category1,category2,category3);
        return BeanHelper.copyWithCollection(list,CategoryDTO.class);
    }
}
