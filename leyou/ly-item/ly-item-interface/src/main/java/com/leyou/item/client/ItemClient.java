package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface ItemClient {
    /**
     * 查询并分页
     * http://api.leyou.com/api/item/spu/page?key=&saleable=true&page=1&rows=5
     */
    @GetMapping("spu/page")
    PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows);
    /**
     * 根据spuID查询sku列表
     * /item/sku/of/spu?id
     */
    @GetMapping("sku/of/spu")
    List<SkuDTO> querySkuBySpuId(@RequestParam("id") Long id);

    /**
     * 查询规格参数
     * @param gid  组id
     * @param cid 分类id
     * @param searching  是否用于搜索
     * @return
     */
    @GetMapping("spec/params")
    List<SpecParamDTO> queryParamByGroupId(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)Boolean searching);
    /**
     * 根据spuId查询spuDetail
     * http://api.leyou.com/api/item/spu/detail?id=195
     */
    @GetMapping("spu/detail")
    SpuDetailDTO querySpuDetailById(@RequestParam("id")Long id);

    /**
     * 根据品牌的id集合来查询Brand
     * @param ids
     * @return
     */
    @GetMapping("brand/list")
    List<BrandDTO> queryBrandByIds(@RequestParam("ids")List<Long> ids);

    /**
     * 根据id集合查询分类
     * @param ids
     * @return
     */
    @GetMapping("category/list")
    List<CategoryDTO> queryCategoryByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据spu的id查询spu
     */
    @GetMapping("spu/{id}")
    SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 根据id查询brnad
     * @param id  brandId
     * @return
     */
    @GetMapping("brand/{id}")
    BrandDTO queryById(@PathVariable("id")Long id);

    /**
     *查询规格参数组，及组内参数
     * @param id
     * @return
     */
    @GetMapping("spec/of/category")
    List<SpecGroupDTO> querySpecGroupByCid(@RequestParam("id") Long id);

    /**
     * http://api.leyou.com/api/item/sku/list?ids=27359021560
     * 根据skuId集合查询sku，展示购物车
     */
    @GetMapping("sku/list")
    List<SkuDTO> querySkusByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 减库存
     */
    @PutMapping("stock/minus")
    void minusStock(@RequestBody Map<Long,Integer> cartMap);
}
