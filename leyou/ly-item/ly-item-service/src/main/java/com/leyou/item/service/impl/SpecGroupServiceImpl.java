package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.service.SpecGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Transactional
public class SpecGroupServiceImpl implements SpecGroupService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 根据cid查询规则组
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroupDTO> queryGroupByCategory(Long cid) {
        //直接new对象，通过类型查询
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> specGroupList = specGroupMapper.selectList(new QueryWrapper<>(specGroup));
        //判断是否为空，为空的话，抛出异常
        if (CollectionUtils.isEmpty(specGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specGroupList,SpecGroupDTO.class);
    }


    /**
     * 新增
     * @param specGroupDTO
     * @return
     */
    @Override
    public void saveSpecGroup(SpecGroupDTO specGroupDTO) {
        int count = specGroupMapper.insert(BeanHelper.copyProperties(specGroupDTO, SpecGroup.class));
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Override
    public void updateSpecGroup(SpecGroupDTO specGroupDTO) {
        int count = specGroupMapper.updateById(BeanHelper.copyProperties(specGroupDTO, SpecGroup.class));
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    @Override
    public void deleteSpecGroup(Long id) {
        int count = specGroupMapper.deleteById(id);
        if (count != 1){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(id);
        List<SpecParam> specParamList = specParamMapper.selectList(new QueryWrapper<>(specParam));
        if (!CollectionUtils.isEmpty(specParamList)){
            count = specParamMapper.delete(new QueryWrapper<>(specParam));
            if (!SqlHelper.retBool(count)){
                throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
            }
        }

    }
}
