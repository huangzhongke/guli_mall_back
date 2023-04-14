package com.hzk.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.coupon.dao.SeckillSessionDao;
import com.hzk.gulimall.coupon.entity.SeckillSessionEntity;
import com.hzk.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.hzk.gulimall.coupon.service.SeckillSessionService;
import com.hzk.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().lambda().between(SeckillSessionEntity::getStartTime, startTime(), endTime()));
        //
        if (list != null || list.size()>0){
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> skuRelationList = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().lambda().eq(SeckillSkuRelationEntity::getPromotionSessionId, id));
                session.setRelationSkus(skuRelationList);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    public String startTime(){
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime startDate = LocalDateTime.of(now, min);
        String start = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return start;
    }
    public String endTime(){
        LocalDate now = LocalDate.now();
        LocalDate localDate = now.plusDays(2);
        LocalTime MAX = LocalTime.MAX;
        LocalDateTime endDate = LocalDateTime.of(localDate, MAX);
        String end = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return end;
    }


}