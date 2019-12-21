package com.leyou.item.dto;

import lombok.Data;

import java.util.List;


@Data
public class SpecGroupDTO {
    private Long id;

    private Long cid;

    private String name;

    private List<SpecParamDTO> params;  //规格组中对应的规格参数集合
}