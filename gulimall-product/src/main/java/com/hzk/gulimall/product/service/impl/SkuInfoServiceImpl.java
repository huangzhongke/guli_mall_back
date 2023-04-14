package com.hzk.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.to.SkuHasStockVo;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.common.utils.R;
import com.hzk.gulimall.product.dao.SkuInfoDao;
import com.hzk.gulimall.product.entity.SkuImagesEntity;
import com.hzk.gulimall.product.entity.SkuInfoEntity;
import com.hzk.gulimall.product.entity.SpuInfoDescEntity;
import com.hzk.gulimall.product.feign.SeckillFeignService;
import com.hzk.gulimall.product.feign.WareFeignService;
import com.hzk.gulimall.product.service.*;
import com.hzk.gulimall.product.vo.SeckillSkuVo;
import com.hzk.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /**
         * key:
         * catelogId: 225
         * brandId: 2
         * min: 0
         * max: 0
         */
        LambdaQueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<SkuInfoEntity>().lambda();
        String key = params.get("key").toString();
        String catelogId = params.get("catelogId").toString();
        String brandId = params.get("brandId").toString();
        String min = params.get("min").toString();
        String max = params.get("max").toString();
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq(SkuInfoEntity::getSkuId, key).eq(SkuInfoEntity::getSkuName, key);
            });
        }
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq(SkuInfoEntity::getCatalogId, catelogId);
        }
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq(SkuInfoEntity::getBrandId, brandId);
        }
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                    wrapper.le(SkuInfoEntity::getPrice, max);
                }
            } catch (Exception e) {

            }
        }
        if (!StringUtils.isEmpty(min)) {
            wrapper.gt(SkuInfoEntity::getPrice, min);
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper

        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        return this.baseMapper.selectList(new QueryWrapper<SkuInfoEntity>().lambda().eq(SkuInfoEntity::getSpuId, spuId));
    }

    @Override
    public SkuItemVo item(Long skuId) {
        SkuItemVo skuItemVo = new SkuItemVo();


        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVo.setInfo(skuInfo);
            return skuInfo;
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity desc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(desc);
        }, executor);


        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getCatalogId(), res.getSpuId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SkuItemSaleAttrVo> saleAttr = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttr);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSkuSeckillInfo(skuId);
            if (r.getCode() == 0) {
                SeckillSkuVo seckillSkuVo = r.getData(new TypeReference<SeckillSkuVo>() {
                });
                skuItemVo.setSeckillSkuVo(seckillSkuVo);
            }
        }, executor);


        CompletableFuture<Void> stockFuture = CompletableFuture.runAsync(() -> {
            R r = wareFeignService.getSkuHasStock(Arrays.asList(skuId));
            if (r.getCode() == 0){
                List<SkuHasStockVo> data = r.getData(new TypeReference<List<SkuHasStockVo>>() {
                });
                SkuHasStockVo skuHasStockVo = data.get(0);
                skuItemVo.setHasStock(skuHasStockVo.getHasStock());
            }
        }, executor);
        try {
            CompletableFuture.allOf(descFuture,baseAttrFuture,saleAttrFuture,imageFuture,stockFuture,seckillFuture).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return skuItemVo;
    }


}