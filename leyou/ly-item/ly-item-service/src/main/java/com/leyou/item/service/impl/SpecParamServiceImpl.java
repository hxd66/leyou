package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
@Service
@Transactional
public class SpecParamServiceImpl implements SpecParamService {

    @Autowired
    private SpecParamMapper specParamMapper;
    @Override
    public List<SpecParamDTO> queryParamByGroupId(Long gid, Long cid, Boolean searching) {
        //gid和cid必选一个
        if (gid == null && cid ==null){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        //封装查询条件
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(gid);
        specParam.setSearching(searching);
        List<SpecParam> specParamList = specParamMapper.selectList(new QueryWrapper<>(specParam));
        if (CollectionUtils.isEmpty(specParamList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specParamList,SpecParamDTO.class);
    }

    /**
     * 新增参数
     * @param specParamDTO
     */
    @Override
    public void saveSpecParam(SpecParamDTO specParamDTO) {
        int count = specParamMapper.insert(BeanHelper.copyProperties(specParamDTO, SpecParam.class));
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 修改
     * @param specParamDTO
     */
    @Override
    public void updateSpecParam(SpecParamDTO specParamDTO) {
        //先判断是否为数值，如果不是，把单位设为空
        if (!specParamDTO.getIsNumeric()){
            specParamDTO.setUnit("");
        }
        int count = specParamMapper.updateById(BeanHelper.copyProperties(specParamDTO, SpecParam.class));
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void deleteSpecParam(Long id) {
        int count = specParamMapper.deleteById(id);
        if (count != 1){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }


}
