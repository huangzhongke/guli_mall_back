package com.hzk.gulimall.ware.controller;

import com.hzk.common.exception.BizCodeEnum;
import com.hzk.common.exception.NoStockException;
import com.hzk.common.to.SkuHasStockVo;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.R;
import com.hzk.gulimall.ware.entity.WareSkuEntity;
import com.hzk.gulimall.ware.service.WareSkuService;
import com.hzk.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 10:06:17
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    @GetMapping("/order/finish")
    public R orderStockMinus(@RequestParam("orderSn") String orderSn){
        wareSkuService.orderStockMinus(orderSn);
        return R.ok();
    }

    @PostMapping("/lock/order")
    public R orderStockLock(@RequestBody WareSkuLockVo vo){
        try {
            Boolean hasStock = wareSkuService.orderStockLock(vo);
            return R.ok().setData(hasStock);
        } catch (NoStockException e) {
            return R.error(BizCodeEnum.NO_STOCK_EXCEPTION.getCode(),BizCodeEnum.NO_STOCK_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
       List<SkuHasStockVo> vo = wareSkuService.getSkuHasStock(skuIds);
        R ok = R.ok();
        ok.setData(vo);
        return ok;
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
