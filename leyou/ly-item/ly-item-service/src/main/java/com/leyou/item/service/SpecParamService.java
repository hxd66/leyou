package com.leyou.item.service;

import com.leyou.item.dto.SpecParamDTO;

import java.util.List;

public interface SpecParamService {
    List<SpecParamDTO> queryParamByGroupId(Long gid, Long cid, Boolean searching);

    void saveSpecParam(SpecParamDTO specParamDTO);

    void updateSpecParam(SpecParamDTO specParamDTO);

    void deleteSpecParam(Long id);
}
