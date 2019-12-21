package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;
    /**
     * "'/item/' + goods.id + '.html'"
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        //查询模型数据
        Map<String,Object> map = pageService.loadItemData(id);
        //存入模型数据，因为数据较多，直接存入map
        model.addAllAttributes(map);
        return "item";
    }
}
