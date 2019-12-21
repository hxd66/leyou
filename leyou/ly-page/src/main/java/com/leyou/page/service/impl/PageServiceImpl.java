package com.leyou.page.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageServiceImpl implements PageService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Value("${ly.static.itemDir}")
    private String itemDir;
    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;
    @Override
    public Map<String, Object> loadItemData(Long id) {
        //查询spu
        SpuDTO spuDTO = itemClient.querySpuById(id);
        //查询集合分类
        List<CategoryDTO> categories = itemClient.queryCategoryByIds(spuDTO.getCategoryIds());
        //查询品牌
        BrandDTO brand = itemClient.queryById(spuDTO.getBrandId());
        //查询规格
        List<SpecGroupDTO> specGroups = itemClient.querySpecGroupByCid(spuDTO.getCid3());
        //封装数据

        Map<String,Object> data = new HashMap<>();
        data.put("categories",categories);
        data.put("brand",brand);
        data.put("spuName",spuDTO.getName());
        data.put("subTitle",spuDTO.getSubTitle());
        data.put("skus",spuDTO.getSkus());
        data.put("detail", spuDTO.getSpuDetail());
        data.put("specs", specGroups);
        return data;
    }

    public void createItemHtml(Long id){
        //上下文，准备模型数据
        Context context = new Context();
        //调用之前的方法加载数据
        context.setVariables(loadItemData(id));
        //准备目录
        File dir = new File(itemDir);
        if (!dir.exists()){
            if (!dir.mkdirs()){
                //创建失败，抛出异常
                log.error("【静态页服务】创建静态页目录失败，目录地址：{}", dir.getAbsolutePath());
                throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
            }
        }

        //准备输出文件的路径
        File filePath = new File(dir, id + ".html");
        //PrintWriter输出文件
        try(PrintWriter writer = new PrintWriter(filePath,"UTF-8")){
            springTemplateEngine.process(itemTemplate,context,writer);
        } catch (IOException e) {
            log.error("【静态页服务】静态页生成失败，商品id：{}", id, e);
            throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }
    }

    /**
     * 商品下架，删除静态页面
     * @param id
     */
    @Override
    public void deleteItemHtml(Long id) {
        File file = new File(itemDir, id + ".html");
        if (file.exists()){
            if (!file.delete()){
                log.error("【静态页服务】静态页删除失败，商品id：{}", id);
                throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
            }
        }
    }

}
