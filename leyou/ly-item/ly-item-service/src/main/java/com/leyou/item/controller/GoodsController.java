package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 商品上下架
     * http://api.leyou.com/api/item/spu/saleable
     */
    @PutMapping("spu/saleable")
    public ResponseEntity<Void> updateSaleable(@RequestParam("id")Long id,
                                               @RequestParam("saleable") Boolean saleable){
        goodsService.updateSaleable(id,saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spuId查询spuDetail
     * http://api.leyou.com/api/item/spu/detail?id=195
     */
    @GetMapping("spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailById(@RequestParam("id")Long id){
        return ResponseEntity.ok(goodsService.querySpuDetailById(id));
    }

    /**
     * 根据spuID查询sku列表
     * /item/sku/of/spu?id
     */
    @GetMapping("sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkuBySpuId(@RequestParam("id") Long id){
        return ResponseEntity.ok(goodsService.querySkuBySpuId(id));
    }

    /**
     * http://api.leyou.com/api/item/goods
     */
    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * http://api.leyou.com/api/item/spu/delete/?id=3
     */
    @DeleteMapping("spu/delete")
    public ResponseEntity<Void> deleteGoods(@RequestParam("id") Long id){
        goodsService.deleteGoods(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spu的id查询spu
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }
}
