package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;

import java.util.List;

public interface SpecGroupService {
    List<SpecGroupDTO> queryGroupByCategory(Long cid);

    void saveSpecGroup(SpecGroupDTO specGroupDTO);

    void updateSpecGroup(SpecGroupDTO specGroupDTO);

    void deleteSpecGroup(Long id);

    List<SpecGroupDTO> querySpecGroupByCid(Long id);
}
