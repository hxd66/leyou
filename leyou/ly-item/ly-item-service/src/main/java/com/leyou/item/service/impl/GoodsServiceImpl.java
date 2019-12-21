package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.Spu;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper detailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * @param key  模糊查询的条件
     * @param saleable  判断是否上架
     * @param page  第几页，默认1
     * @param rows  每页显示的条数，默认5
     * @return
     */
    @Override
    public PageResult<SpuDTO> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        //分页查询，封装分页参数
        Page<Spu> spuPage = new Page<>(page, rows);
        QueryWrapper<Spu> wrapper = new QueryWrapper<>();
        //模糊查询条件
        wrapper.like(StringUtils.isNotBlank(key),"name",key);
        //封装是否上架条件
        wrapper.eq(saleable != null,"saleable",saleable);
        //查询
        spuMapper.selectPage(spuPage,wrapper);
        //类转换
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(spuPage.getRecords(), SpuDTO.class);
        //判断结果是否为空
        if (CollectionUtils.isEmpty(spuPage.getRecords())){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        handleCategoryAndBrandName(spuDTOList);
        return new PageResult<>(spuPage.getTotal(),spuDTOList);
    }

    /**
     * 这里要添加spu的话，也需要把sku和spu_detial添加了
     * @param spuDTO
     */
    @Override
    public void saveSpu(SpuDTO spuDTO) {
        //开始新增商品
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setSaleable(false);   //是否上架
        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //对spuDetail的初始化
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetail.setSpuId(spu.getId());
        count = detailMapper.insert(spuDetail);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        count = 0;  //初始化count
        //对sku进行初始化
        List<SkuDTO> skuDTOS = spuDTO.getSkus();
        for (SkuDTO skuDTO : skuDTOS) {
            skuDTO.setSpuId(spu.getId());
            Sku sku = BeanHelper.copyProperties(skuDTO, Sku.class);
            count = skuMapper.insert(sku) + count;
        }
        if (count != skuDTOS.size() || count == 0){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 商品的上下架
     * @param id spu的id
     * @param saleable 是否上下架
     */
    @Override
    public void updateSaleable(Long id, Boolean saleable) {
        //先更新spu
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(saleable);
        int count = spuMapper.updateById(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //然后根据spu_id更新sku
        Sku sku = new Sku();
        sku.setEnable(saleable);
        QueryWrapper<Sku> wrapper = new QueryWrapper<>();
        wrapper.eq(id != null,"spu_id",id);
        int update = skuMapper.update(sku, wrapper);
        //SqlHelper是Mybatisplus内部提供的一个方法，可以查看是否修改成功
        boolean bool = SqlHelper.retBool(update);
        if (!bool){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //新增发送mq消息
        String key = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;
        //1.交换机  2.路由  3.消息内容
        amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME,key,id);
    }

    /**
     * 根据id查询spuDetail
     * @param id spu_id
     * @return
     */
    @Override
    public SpuDetailDTO querySpuDetailById(Long id) {
        SpuDetail spuDetail = detailMapper.selectById(id);
        if (spuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail,SpuDetailDTO.class);
    }

    /**
     * 根据spu_id查询对应的sku
     * @param id spu_id
     * @return
     */
    @Override
    public List<SkuDTO> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.selectList(new QueryWrapper<>(sku));
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH_ERROR);
        }
        return BeanHelper.copyWithCollection(skuList,SkuDTO.class);
    }

    /**
     * 修改保存
     * @param spuDTO
     */
    @Override
    public void updateGoods(SpuDTO spuDTO) {
        //先转换
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        //设置修改时间
        spu.setUpdateTime(new Date());
        //判断是否修改成功
        int count = spuMapper.updateById(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //修改spuDetail
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        count = detailMapper.updateById(BeanHelper.copyProperties(spuDetailDTO,SpuDetail.class));
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //修改sku，修改前先删除
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        count = 0;
        //判断是否删除成功
        count = skuMapper.delete(new QueryWrapper<>(sku));
        if (!SqlHelper.retBool(count)){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        //删除完了，再新增
        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        //转下一下
        List<Sku> skuList = BeanHelper.copyWithCollection(skuDTOList, Sku.class);
        //判断是否新增成功
        count = 0;
        for (Sku sku1 : skuList) {
            sku1.setSpuId(spu.getId());
            skuMapper.insert(sku1);
            count++;
        }
        if (count != skuList.size()){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    /**
     * 删除商品
     * @param id
     */
    @Override
    public void deleteGoods(Long id) {
        int count = spuMapper.deleteById(id);
        if (count != 1){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        SpuDetail spuDetail = new SpuDetail();
        spuDetail.setSpuId(id);
        count = detailMapper.delete(new QueryWrapper<>(spuDetail));
        if (count != 1){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        Sku sku = new Sku();
        sku.setSpuId(id);
        count = 0;
        count = skuMapper.delete(new QueryWrapper<>(sku));
        if (!SqlHelper.retBool(count)){
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
    }

    @Override
    public SpuDTO querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectById(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        //查询spuDetail
        spuDTO.setSpuDetail(querySpuDetailById(id));
        //查询sku
        spuDTO.setSkus(querySkuBySpuId(id));
        return spuDTO;
    }

    /**
     * 封装分类列表和品牌名字
     @param spuDTOList
     */
    private void handleCategoryAndBrandName(List<SpuDTO> spuDTOList){
        for (SpuDTO spuDTO : spuDTOList) {
            List<Long> categoryIds = spuDTO.getCategoryIds();
            //拼接字符串
            String categoryNames = categoryService.queryCategoryByIds(categoryIds).stream()
                    .map(CategoryDTO::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryNames);
            //查询品牌
            BrandDTO brandDTO = brandService.queryBrandById(spuDTO.getBrandId());
            //设置spuDTO属性
            spuDTO.setBrandName(brandDTO.getName());
        }
    }
}
