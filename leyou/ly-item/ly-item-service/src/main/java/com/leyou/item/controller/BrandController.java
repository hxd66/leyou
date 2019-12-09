package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
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
    @GetMapping("page")
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

    @PostMapping
    public ResponseEntity<Void> saveBrand(BrandDTO brandDTO,
                                          @RequestParam("cids")List<Long> ids){
        brandService.saveBrand(brandDTO,ids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
