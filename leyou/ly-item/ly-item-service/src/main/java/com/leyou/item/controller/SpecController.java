package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SpecController {
    @Autowired
    private SpecService specService;

    /**
     * 根据cid查询规格组
     * http://api.leyou.com/api/item/spec/groups/of/category/?id=76
     * @param cid
     * @return
     */
    @GetMapping("spec/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupByCategory(@RequestParam("id") Long cid){
        return ResponseEntity.ok(specService.queryGroupByCategory(cid));
    }

    /**
     * http://api.leyou.com/api/item/spec/params?gid=1
     */
    @GetMapping("spec/params")
    public ResponseEntity<List<SpecParamDTO>> queryParamByGroupId(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching){
        return ResponseEntity.ok(specService.queryParamByGroupId(gid,cid,searching));
    }

    /**
     * http://api.leyou.com/api/item/spec/group
     */
    @PostMapping("spec/group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroupDTO specGroupDTO){
        specService.saveSpecGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
