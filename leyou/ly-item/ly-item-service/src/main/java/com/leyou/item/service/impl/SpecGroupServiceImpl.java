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
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpecGroupServiceImpl implements SpecGroupService {
    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    @Autowired
    private SpecParamService specParamService;
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

    @Override
    public List<SpecGroupDTO> querySpecGroupByCid(Long id) {
        //查询规格组，调用之前的方法
        List<SpecGroupDTO> specGroupDTOS = queryGroupByCategory(id);
        //查询分类下所有规格参数，调用之前方法
        List<SpecParamDTO> specParamDTOS = specParamService.queryParamByGroupId(null, id, null);

        //将规格参数按照groupId进行分组，得到每个group下的param的集合
        Map<Long, List<SpecParamDTO>> paramMap = specParamDTOS.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));

        //先for规格组嵌套for规格参数，判断规格组id是规格参数中组id一致将规格参数对象加入规格组准备的几何中
        //填写到group中，根据分足后的id获取param
        for (SpecGroupDTO groupDTO : specGroupDTOS) {
            groupDTO.setParams(paramMap.get(groupDTO.getId()));
        }

        return specGroupDTOS;
    }
}
