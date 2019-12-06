package com.leyou.service;

import com.leyou.pojo.Item;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ItemService {
    public Item save(Item item){
        int id = new Random().nextInt();
        item.setId(id);
        return item;
    }
}
