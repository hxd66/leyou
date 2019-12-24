package com.leyou.item.controller;

import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecGroupService;
import com.leyou.item.service.SpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RefreshScope
public class SpecParamController {
    @Autowired
    private SpecParamService specParamService;

    /**
     * 查询规格参数
     * @param gid  组id
     * @param cid 分类id
     * @param searching  是否用于搜索
     * @return
     */
    @GetMapping("spec/params")
    public ResponseEntity<List<SpecParamDTO>> queryParamByGroupId(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching){
        return ResponseEntity.ok(specParamService.queryParamByGroupId(gid,cid,searching));
    }

    /**
     * http://api.leyou.com/api/item/spec/param
     */
    @PostMapping("spec/param")
    public ResponseEntity<Void> saveSpecParam(@RequestBody SpecParamDTO specParamDTO){
        specParamService.saveSpecParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * http://api.leyou.com/api/item/spec/param
     */
    @PutMapping("spec/param")
    public ResponseEntity<Void> updateSpecParam(@RequestBody SpecParamDTO specParamDTO){
        specParamService.updateSpecParam(specParamDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * http://api.leyou.com/api/item/spec/param/30
     */
    @DeleteMapping("spec/param/{id}")
    public ResponseEntity<Void> deleteSpecParam(@PathVariable("id")Long id){
        specParamService.deleteSpecParam(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
