package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.entity.Spu;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;

    /**
     * @param key  模糊查询的条件
     * @param saleable  判断是否上架
     * @param page  第几页，默认1
     * @param rows  每页显示的条数，默认5
     * @return
     */
    @Override
    public PageResult<SpuDTO> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        //分页查询，封装分页参数
        Page<Spu> spuPage = new Page<>(page, rows);
        QueryWrapper<Spu> wrapper = new QueryWrapper<>();
        //模糊查询条件
        wrapper.like(StringUtils.isNotBlank(key),"name",key);
        //封装是否上架条件
        wrapper.eq(saleable != null,"saleable",saleable);
        //查询
        spuMapper.selectPage(spuPage,wrapper);
        //类转换
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(spuPage.getRecords(), SpuDTO.class);
        //判断结果是否为空
        if (CollectionUtils.isEmpty(spuPage.getRecords())){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        handleCategoryAndBrandName(spuDTOList);
        return new PageResult<>(spuPage.getTotal(),spuDTOList);
    }

    /**
     * 封装分类列表和品牌名字
     @param spuDTOList
     */
    private void handleCategoryAndBrandName(List<SpuDTO> spuDTOList){
        for (SpuDTO spuDTO : spuDTOList) {
            List<Long> categoryIds = spuDTO.getCategoryIds();
            String categoryNames = categoryService.queryCategoryByIds(categoryIds).stream()
                    .map(CategoryDTO::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryNames);
            Brand brand = brandService.queryBrandById(spuDTO.getBrandId());
            spuDTO.setBrandName(brand.getName());
        }
    }
}
