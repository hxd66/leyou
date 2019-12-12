package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @GetMapping("category/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(@RequestParam("pid")Long pid){
        //返回一个ResponseEntity对象
        return ResponseEntity.ok(categoryService.queryByParentId(pid));
    }

    /**
     * 根据品牌查询商品分类
     * http://api.leyou.com/api/item/category/of/brand/?id=2032
     */
    @GetMapping("category/of/brand")
    public ResponseEntity<List<CategoryDTO>> queryByBrandId(@RequestParam("id")Long bid){
        return ResponseEntity.ok(categoryService.queryByBrandId(bid));
    }
}
