package com.leyou.search.dto;

import lombok.Data;

@Data
public class GoodsDTO {
    private Long id;   //spuid
    private String subTitle;   //卖点，副标题
    private String skus;  //sku信息的json格式
}
