package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RefreshScope
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

    /**
     * 根据id集合查询分类
     * @param ids
     * @return
     */
    @GetMapping("category/list")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryCategoryByIds(ids));
    }

    /**
     * 根据3级分类的id，查询1-3级的分类
     * @param id
     * @return
     */
    @GetMapping("category/levels")
    public ResponseEntity<List<CategoryDTO>> queryAllByCid3(@RequestParam("id") Long id){
        return ResponseEntity.ok(categoryService.queryAllByCid3(id));
    }


}
