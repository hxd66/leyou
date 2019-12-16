package com.leyou.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ItemClient itemClient;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 把一个Spu转为Goods
     * @param spuDTO
     * @return
     */
    @Override
    public Goods buildGoods(SpuDTO spuDTO) {
        //1.构建all的String字符串，名称+ 分类名称 + 品牌名称
        String allStr = spuDTO.getName() + spuDTO.getCategoryName() + spuDTO.getBrandName();
        //2.简化skus的例表信息
        List<Map<String,Object>> skuMaps = new ArrayList<>();
        //我们只需要sku里的四个参数信息，而且不用于搜索字段，所以直接转换为json字符串，存入索引库
        List<SkuDTO> skuDTOList = itemClient.querySkuBySpuId(spuDTO.getId());
        for (SkuDTO skuDTO : skuDTOList) {
            Map<String,Object> map = new HashMap<>();
            map.put("id",skuDTO.getId());
            map.put("title",skuDTO.getTitle());
            map.put("price",skuDTO.getPrice());
            map.put("image",skuDTO.getImages());
            skuMaps.add(map);
        }

        //3.构建价格的set集合
        Set<Long> priceSet = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());

        //4.构建spu对应的规格组参数
        Map<String,Object> specs = new HashMap<>();

        //根据spuId查询出SpuDetail
        SpuDetailDTO spuDetailDTO = itemClient.querySpuDetailById(spuDTO.getId());
//        {"1":"其它","2":"畅享6","3":2016.0,"5":145,"6":"其它","16":500.0,"17":1300.0,"18":4100}
        //将通用属性转为Map
        Map<Long, Object> genericSpecMap = JsonUtils.nativeRead(spuDetailDTO.getGenericSpec(), new TypeReference<Map<Long, Object>>() {
        });
        //将自定义属性转为Map
        Map<Long, Object> specialSpecMap = JsonUtils.nativeRead(spuDetailDTO.getSpecialSpec(), new TypeReference<Map<Long, Object>>() {
        });
        //根据分类三级id查询规格参数
        List<SpecParamDTO> specParamDTOS = itemClient.queryParamByGroupId(null, spuDTO.getCid3(), true);

        //遍历规格参数集合
        for (SpecParamDTO specParamDTO : specParamDTOS) {
            //参数名作为key
            String key = specParamDTO.getName();
            Object value = null;
            if (genericSpecMap != null && specialSpecMap != null){
                //判断是通用属性还是非通用属性
                if (specParamDTO.getGeneric()){
                    //根据参数id来获取对应的属性
                    value = genericSpecMap.get(specParamDTO.getId());
                }else {
                    value = specialSpecMap.get(specParamDTO.getId());
                }
            }
//            再判断是否为数字类型
            if (specParamDTO.getIsNumeric()){
                //是的话，进行处理
                value = chooseSegment(value,specParamDTO);
            }
            specs.put(key,value);

        }

        Goods goods = new Goods();
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setId(spuDTO.getId());
        goods.setSpecs(specs);
        goods.setPrice(priceSet);
        goods.setSkus(JsonUtils.toString(skuMaps));
        goods.setAll(allStr);
        return goods;
    }
    /**
     * 1.判断关键字是否有值，没有抛异常
     * 2.new NativeSearchQueryBuilder原生查询构造器
     * 3.构造器中通过SourceFilter只抓取"id","subTitle","skus"字段内容
     * 4.构造器中设置查询条件，采用matchQuery，查询采用并且关系
     * 5.设置分页参数PageRequest.of(pageNo-1,pageSize)
     * 6.查询获取Goods结果,索引库保存的都是Goods
     * 7.PageResult拼接返回结果
     */
    @Override
    public PageResult<GoodsDTO> search(SearchRequest searchRequest) {
        //1.判断关键字是否有值，没有抛异常
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key))
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        //2.new NativeSearchQueryBuilder原生查询构造器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //3.构造器中通过sourceFilter只抓取"id","subTitle","skus"字段内容
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //4.封装查询条件，采用matchQuery，查询采用并且用AND
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key).operator(Operator.AND));
        queryBuilder.withQuery(buildBaseQuery(searchRequest));
        //5.设置分页参数PageRequest.of(pageNo-1,pageSize)
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));
        //6.查询获取Goods结果，索引库保存的都是Goods
        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);

        long totalElements = goods.getTotalElements();   //总记录条数
        int totalPages = goods.getTotalPages();   //总分页数
        //转为GoodDTO
        List<GoodsDTO> goodsDTOS = BeanHelper.copyWithCollection(goods.getContent(), GoodsDTO.class);
        return new PageResult<>(totalElements,totalPages,goodsDTOS);
    }

    //将查询条件先抽取成方法，后续方便扩展
    private QueryBuilder buildBaseQuery(SearchRequest searchRequest){

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //关键字查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        //过滤查询条件
        Map<String, String> filter = searchRequest.getFilter();

        for (String key : filter.keySet()) {
            //将前端传入的查询条件分类和品牌替换成索引库中域名
            String name = "";
            if ("分类".equals(key)){
                name = "categoryId";
            }else if ("品牌".equals(key)){
                name = "brandId";
            }else {
                name = "specs." + key;
            }
            queryBuilder.filter(QueryBuilders.termQuery(name,filter.get(key)));

        }
        return queryBuilder;
    }

    @Override
    public Map<String, List> queryFilters(SearchRequest searchRequest) {
        //因为页面显示需要有序显示，所以用LinkedHashMap
        Map<String,List> filterHash = new LinkedHashMap<>();
        //构建基本的查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //设置查询条件
        queryBuilder.withQuery(buildBaseQuery(searchRequest));
        //设置分页参数，不想要查询后的结果，只想看分桶后的结果，必须将size设为1，否则api报错
        queryBuilder.withPageable(PageRequest.of(0,1));
        //过滤查询结果，只拿需要的字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(null,null));
        //准备聚合条件，terms（指定桶名）  .field（字段名称）
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId"));
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId"));
        //查询
        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        //根据桶名称获取分桶结果
        Terms categoryAgg = (Terms)goods.getAggregation("categoryAgg");
        //拿到分类id集合
        List<Long> cidList = handleCategoryAgg(categoryAgg, filterHash);
        //根据桶名称查询到分桶结果
        Terms brandAgg = (Terms)goods.getAggregation("brandAgg");
        //调用方法将品牌名称存入map
        handleBrandAgg(brandAgg,filterHash);
        //当分类id只有一个时，查询出对应的规格参数，因为多了的话，给用户的体验不好
        if (cidList!=null && cidList.size() == 1){
            //根据组装好的查询条件和cid进行封装
            handleSpecAgg(cidList.get(0),buildBaseQuery(searchRequest),filterHash);
        }

        return filterHash;
    }

    /**
     * 封装规格参数
     * @param cid
     * @param baseQuery
     * @param filterHash
     */
    private void handleSpecAgg(Long cid, QueryBuilder baseQuery, Map<String, List> filterHash) {
        //构建基本查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //关键字查询
        queryBuilder.withQuery(baseQuery);
        //封装分页参数
        queryBuilder.withPageable(PageRequest.of(0,1));
        //不需要查询后的结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(null,null));
        //根据分类id获取对应的参数列表
        List<SpecParamDTO> specParamDTOS = itemClient.queryParamByGroupId(null, cid, true);
        //设置分桶查询的条件
        for (SpecParamDTO specParamDTO : specParamDTOS) {
            String name = specParamDTO.getName();

            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));

        }
        //查询出对应的商品
        AggregatedPage<Goods> goods = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        //循环所有的规格参数列表通过名称获取桶中数据
        for (SpecParamDTO specParamDTO : specParamDTOS) {
            //对应map中的key
            String key = specParamDTO.getName();
            //根据桶的名称后去对应的key组成的list集合
            Terms terms = (Terms) goods.getAggregation(key);
            //通过流逝编程将value转为String再组成list
            List<String> value = terms.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
            //存入过滤选项中，作为规格参数过滤选项
            filterHash.put(key,value);
        }
    }

    /**
     * 根据分桶后的结果组装分类的过滤项
     * @param terms categoryAgg
     * @param filterHash
     * @return
     */
    private List<Long> handleCategoryAgg(Terms terms, Map<String, List> filterHash) {
        //通过流逝编程将桶中的key收集成number类型，再转成long类型后，组成list
        List<Long> cidList = terms.getBuckets().stream().map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        //远程调用，根据分类id集合查询出分类集合
        List<CategoryDTO> categoryDTOS = itemClient.queryCategoryByIds(cidList);
        //存入map中
        filterHash.put("分类",categoryDTOS);

        return cidList;
    }

    /**
     * 将品牌名称存入map
     * @param terms
     * @param filterHash
     */
    private void handleBrandAgg(Terms terms, Map<String, List> filterHash) {
        //通过流逝编程将桶中的key收集成number类型再转成long类型后，组成list
        List<Long> idList = terms.getBuckets().stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        //通过远程调用根据品牌id查询出品牌集合
        List<BrandDTO> brandDTOS = itemClient.queryBrandByIds(idList);
        //将品牌集合存入map
        filterHash.put("品牌",brandDTOS);
    }

    /**
     * 处理数值是这种类型的0-1.0,1.0-1.5,1.5-2.0,2.0-2.5,2.5-
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(Object value,SpecParamDTO p){
        //判断有没有对应的value值，没有的话，分为其它
        if (value == null || StringUtils.isBlank(value.toString())){
            return "其它";
        }
        //将value转换为double类型
        double val = parseDouble(value.toString());
        String result = "其它";
        //因为该字符串是区间数值类型的，所以需要对其进行切割
        //3000mAh-3499mAh，3500mAh-3749mAh，4000mAh-4249mAh，5000mAh以上
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            //取出开始值
            double begin = parseDouble(segs[0]);
            //先自定义结束值，double的最大值
            double end = Double.MAX_VALUE;
            //如果是这样的3000mAh-3499mAh，结束值就取第二个值
            if (segs.length == 2){
                end = parseDouble(segs[1]);
            }
            //判断值是否是在开始值和结束值中间
            if (val >= begin && val < end){
                //只有开始值，拼接字符串
                if (segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                    //初始值为0，拼接字符串
                }else if (begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                    //否则，就直接取3000mAh-3499mAh这种类型的
                }else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 将数值字符串转换为double类型
     * @param str
     * @return
     */
    private double parseDouble(String str){
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
