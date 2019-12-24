package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RefreshScope
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 搜索
     * @param searchRequest  包含搜索条件的对象
     * @return
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<GoodsDTO>> search(@RequestBody SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.search(searchRequest));
    }

    /**
     * 过滤搜索
     * http://api.leyou.com/api/search/filter
     */
    @PostMapping("filter")
    public ResponseEntity<Map<String, List>> queryFilters(@RequestBody SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.queryFilters(searchRequest));
    }
}
