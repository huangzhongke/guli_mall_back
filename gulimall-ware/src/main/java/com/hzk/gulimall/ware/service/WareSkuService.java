package com.hzk.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzk.common.to.OrderTo;
import com.hzk.common.to.SkuHasStockVo;
import com.hzk.common.to.mq.StockLockedTo;
import com.hzk.common.utils.PageUtils;
import com.hzk.gulimall.ware.entity.WareSkuEntity;
import com.hzk.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 10:06:17
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    WareSkuEntity getBySkuId(Long skuId);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    void unlockStock(StockLockedTo stockLockedTo);

    Boolean orderStockLock(WareSkuLockVo vo);

    void unlockStock(OrderTo orderTo);
}

