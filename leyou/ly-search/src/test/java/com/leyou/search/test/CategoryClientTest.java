package com.leyou.search.test;

import com.leyou.LySearchApplication;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class CategoryClientTest {
    @Autowired
    private ItemClient itemClient;
    @Test
    public void testClient(){
        PageResult<SpuDTO> spuByPage = itemClient.querySpuByPage(null, true, 1, 10);
        List<SpuDTO> items = spuByPage.getItems();
        for (SpuDTO item : items) {
            System.out.println(item);
        }
    }
}
