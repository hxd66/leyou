package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SpecGroupController {
    @Autowired
    private SpecGroupService specGroupService;

    /**
     * 根据cid查询规格组
     * http://api.leyou.com/api/item/spec/groups/of/category/?id=76
     * @param cid
     * @return
     */
    @GetMapping("spec/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupByCategory(@RequestParam("id") Long cid){
        return ResponseEntity.ok(specGroupService.queryGroupByCategory(cid));
    }


    /**
     * http://api.leyou.com/api/item/spec/group
     */
    @PostMapping("spec/group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroupDTO specGroupDTO){
        specGroupService.saveSpecGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * http://api.leyou.com/api/item/spec/group
     */
    @PutMapping("spec/group")
    public ResponseEntity<Void> updateSpecGroup(@RequestBody SpecGroupDTO specGroupDTO){
        specGroupService.updateSpecGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * http://api.leyou.com/api/item/spec/group/15
     */
    @DeleteMapping("spec/group/{id}")
    public ResponseEntity<Void> deleteSpecGroup(@PathVariable("id")Long id){
        specGroupService.deleteSpecGroup(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
