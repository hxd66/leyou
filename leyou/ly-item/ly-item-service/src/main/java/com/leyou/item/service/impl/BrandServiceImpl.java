package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;
    @Override
    public PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String key, String sortBy, Boolean desc) {
        //通过Page拼接分页参数
        Page<Brand> brandPage = new Page<>(page, rows);
        //准备wrapper查询
        QueryWrapper<Brand> wrapper = new QueryWrapper<>();
        //模糊搜索name列
        wrapper.like(StringUtils.isNotBlank(key),"name",key);
        //判断升降顺序，拼接传入sortBy列
        if (desc){
            wrapper.orderBy(StringUtils.isNotBlank(sortBy),false,sortBy);
        }else {
            wrapper.orderBy(StringUtils.isNotBlank(sortBy),true,sortBy);
        }
//        page         分页查询条件（可以为 RowBounds.DEFAULT）
//queryWrapper 实体对象封装操作类（可以为 null）
        brandMapper.selectPage(brandPage, wrapper);

        //判断结果是否为空
        if (CollectionUtils.isEmpty(brandPage.getRecords())){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //转换BrandDTO集合
        List<BrandDTO> brandDTOS = BeanHelper.copyWithCollection(brandPage.getRecords(), BrandDTO.class);
        return new PageResult<>(brandPage.getTotal(),brandDTOS);
    }

    @Override
    @Transactional
    public void saveBrand(BrandDTO brandDTO, List<Long> ids) {
        //新增品牌
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        int count = brandMapper.insert(brand);
        if (count != 1){
            //新增失败的时候，抛出异常
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //新增品牌和分类的中间表
        count = brandMapper.insertCategoryBrand(brand.getId(),ids);
        //如果新增到中间表的数量和ids的数量不一致说明新增失败
        if (count != ids.size()){
            //新增失败，抛出异常
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }
}
