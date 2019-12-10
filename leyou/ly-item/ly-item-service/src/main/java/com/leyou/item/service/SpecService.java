package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;

import java.util.List;

public interface SpecService {
    List<SpecGroupDTO> queryGroupByCategory(Long cid);

    List<SpecParamDTO> queryParamByGroupId(Long gid, Long cid, Boolean searching);

    void saveSpecGroup(SpecGroupDTO specGroupDTO);
}
