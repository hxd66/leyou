package com.leyou.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.pojo.Item;
import com.leyou.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {
    @Autowired
    private ItemService itemService;

    @PostMapping("item")
    public ResponseEntity<Item> save(@RequestBody Item item){
        //如果价格为空，则抛出异常,返回400状态码，请求参数有误
        if (item.getPrice() == null){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        Item result = itemService.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
