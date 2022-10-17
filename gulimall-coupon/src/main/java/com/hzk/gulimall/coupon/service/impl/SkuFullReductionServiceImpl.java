package com.hzk.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.to.MemberPrice;
import com.hzk.common.to.SkuReductionTo;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.coupon.dao.SkuFullReductionDao;
import com.hzk.gulimall.coupon.entity.MemberPriceEntity;
import com.hzk.gulimall.coupon.entity.SkuFullReductionEntity;
import com.hzk.gulimall.coupon.entity.SkuLadderEntity;
import com.hzk.gulimall.coupon.service.MemberPriceService;
import com.hzk.gulimall.coupon.service.SkuFullReductionService;
import com.hzk.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveReduction(SkuReductionTo skuReductionTo) {
        //sms_sku_full_reduction(满多少减多少 ), sms_sku_ladder(满多少件打折),sms_member_price
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuLadderEntity.getFullCount() > 0) {

            skuLadderService.save(skuLadderEntity);
        }

        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        if (memberPrice != null && memberPrice.size() > 0) {
            List<MemberPriceEntity> list = memberPrice.stream().map((item) -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setMemberLevelName(item.getName());
                memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setAddOther(1);
                return memberPriceEntity;
            }).filter(item -> {
                return item.getMemberPrice().compareTo(BigDecimal.ZERO) == 1;
            }).collect(Collectors.toList());
            memberPriceService.saveBatch(list);
        }
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        if (skuFullReductionEntity.getFullPrice().compareTo(BigDecimal.ZERO) == 1) {

            this.save(skuFullReductionEntity);
        }
    }

}