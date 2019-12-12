package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.entity.Spu;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 查询并分页
     * http://api.leyou.com/api/item/spu/page?key=&saleable=true&page=1&rows=5
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows){
        return ResponseEntity.ok(goodsService.querySpuByPage(key,saleable,page,rows));
    }

    /**
     * 新增商品
     * @param spuDTO spu里包含了所有需要的属性
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveSpu(@RequestBody SpuDTO spuDTO){
        goodsService.saveSpu(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
