package com.hzk.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.hzk.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.hzk.gulimall.coupon.service.SeckillSkuRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<SeckillSkuRelationEntity> lambdaQueryWrapper = new QueryWrapper<SeckillSkuRelationEntity>().lambda();
        String promotionSessionId = (String) params.get("promotionSessionId");
        if (StringUtils.isEmpty(promotionSessionId)){
            lambdaQueryWrapper.eq(SeckillSkuRelationEntity::getPromotionSessionId,promotionSessionId);
        }
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                lambdaQueryWrapper
        );

        return new PageUtils(page);
    }

}