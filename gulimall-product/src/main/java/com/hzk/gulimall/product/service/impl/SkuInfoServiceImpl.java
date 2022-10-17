package com.hzk.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.product.dao.SkuInfoDao;
import com.hzk.gulimall.product.entity.SkuInfoEntity;
import com.hzk.gulimall.product.service.SkuInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

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
      return this.baseMapper.selectList(new QueryWrapper<SkuInfoEntity>().lambda().eq(SkuInfoEntity::getSpuId,spuId));
    }


}