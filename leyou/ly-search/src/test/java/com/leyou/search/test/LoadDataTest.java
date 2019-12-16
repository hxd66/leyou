package com.leyou.search.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LoadDataTest {
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsRepository repository;
    @Autowired
    private ItemClient itemClient;

    @Test
    public void loadData(){
        int page = 1, size = 0;
        do {
            //从数据库里查询出Spu分页集
            PageResult<SpuDTO> spuByPage = itemClient.querySpuByPage(null, true, page, 100);
            //获取到Spu
            List<SpuDTO> items = spuByPage.getItems();
            //转换为Goods集合
            List<Goods> goodsList = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            repository.saveAll(goodsList);
            //page++来循环取数据
            page ++;
            //将每次获取到的Spu集合大小赋值给size
            size = items.size();
            //当size != 100时结束循环
        }while (size == 100);

    }
}
