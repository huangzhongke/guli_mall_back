package com.hzk.gulimall.product.controller;

import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.R;
import com.hzk.gulimall.product.entity.SkuInfoEntity;
import com.hzk.gulimall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;



/**
 * sku信息
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-11 11:13:31
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;


    @GetMapping("/{skuId}/price")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId){
        return skuInfoService.getById(skuId).getPrice();
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageByCondition(params);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/skuList")
    public R skulist(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody SkuInfoEntity skuInfoEntity){
		skuInfoService.save(skuInfoEntity);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
