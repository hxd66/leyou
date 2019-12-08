package com.leyou.item.controller;

import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
//@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @RequestMapping("of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(@RequestParam("pid")Long pid){
        //返回一个ResponseEntity对象
        return ResponseEntity.ok(categoryService.queryByParentId(pid));
    }
}
