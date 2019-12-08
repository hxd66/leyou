package com.leyou.item.service;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;

import java.util.List;

public interface CategoryService {
    //根据父id查询
    List<CategoryDTO> queryByParentId(Long pid);
}
