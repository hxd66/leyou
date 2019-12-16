package com.leyou.item.controller;

import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 查询并分页
     * @param page
     * @param rows
     * @param key
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("brand/page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            //required指定参数是否必填，默认true：必填
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",defaultValue = "false") Boolean desc
    ){
        return ResponseEntity.ok(brandService.queryBrandByPage(page,rows,key,sortBy,desc));
    }

    @PostMapping("brand")
    public ResponseEntity<Void> saveBrand(BrandDTO brandDTO,
                                          @RequestParam("cids")List<Long> ids){
        System.err.println(brandDTO);
        brandService.saveBrand(brandDTO,ids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("brand")
    public ResponseEntity<Void> updateBrand(BrandDTO brandDTO,@RequestParam("cids")List<Long> ids){
        brandService.updateBrand(brandDTO,ids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * http://api.leyou.com/api/item/brand/?id=1115
     */
    @DeleteMapping("brand")
    public ResponseEntity<Void> deleteByBrandId(@RequestParam("id") Long id){
        brandService.deleteByBrandId(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 新增商品时，需要先查询所属品牌分类
     * http://api.leyou.com/api/item/brand/of/category?id=79
     */
    @GetMapping("brand/of/category")
    public ResponseEntity<List<BrandDTO>> queryByGroupId(@RequestParam("id") Long id){
        return ResponseEntity.ok(brandService.queryByGroupId(id));
    }

    /**
     * 根据id查询brnad
     * @param id  brandId
     * @return
     */
    @GetMapping("brand/{id}")
    public ResponseEntity<BrandDTO> queryById(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.queryBrandById(id));
    }

    /**
     * 根据品牌的id集合来查询Brand
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    public ResponseEntity<List<BrandDTO>> queryBrandByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
