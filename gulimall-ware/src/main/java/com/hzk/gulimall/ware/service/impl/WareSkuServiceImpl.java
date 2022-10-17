package com.hzk.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.common.utils.R;
import com.hzk.gulimall.ware.dao.WareSkuDao;
import com.hzk.gulimall.ware.entity.WareSkuEntity;
import com.hzk.gulimall.ware.feign.ProductFeignService;
import com.hzk.gulimall.ware.service.WareSkuService;
import com.hzk.common.to.SkuHasStockVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<WareSkuEntity>().lambda();
        /**
         * skuId:
         * wareId:
         *
         */
        String skuId = params.get("skuId").toString();
        String wareId = params.get("wareId").toString();
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq(WareSkuEntity::getSkuId, skuId);
        }
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper

        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        List<WareSkuEntity> list = this.list(new QueryWrapper<WareSkuEntity>().lambda().eq(WareSkuEntity::getSkuId, skuId).eq(WareSkuEntity::getWareId, wareId));
        if (list == null || list.size() == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                R r = productFeignService.info(skuId);
                Object skuInfo = r.get("skuInfo");
                Map<String, Object> map = (Map<String, Object>) skuInfo;
                if (r.getCode() == 0) {
                    wareSkuEntity.setSkuName(map.get("skuName").toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.save(wareSkuEntity);
        } else {

            wareSkuDao.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public WareSkuEntity getBySkuId(Long skuId) {
        return this.baseMapper.selectOne(new QueryWrapper<WareSkuEntity>().lambda().eq(WareSkuEntity::getSkuId, skuId));

    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            skuHasStockVo.setSkuId(skuId);
            Long stock = this.baseMapper.getSkuStock(skuId);
            skuHasStockVo.setHasStock(stock == null ? false : stock > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;

    }

}