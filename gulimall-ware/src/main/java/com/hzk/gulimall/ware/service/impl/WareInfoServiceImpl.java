package com.hzk.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.common.utils.R;
import com.hzk.gulimall.ware.dao.WareInfoDao;
import com.hzk.gulimall.ware.entity.WareInfoEntity;
import com.hzk.gulimall.ware.feign.MemberFeignService;
import com.hzk.gulimall.ware.service.WareInfoService;
import com.hzk.gulimall.ware.vo.FareVo;
import com.hzk.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    MemberFeignService memberFeignService;





    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<WareInfoEntity>().lambda();

        if (null != params.get("key") && !StringUtils.isEmpty(params.get("key").toString())) {
            String key = params.get("key").toString();
            wrapper.and(w -> {
                w.eq(WareInfoEntity::getId, key)
                        .or().like(WareInfoEntity::getName, key)
                        .or().like(WareInfoEntity::getAddress, key)
                        .or().eq(WareInfoEntity::getAreacode, key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper

        );

        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R info = memberFeignService.addrInfo(addrId);
        MemberAddressVo memberAddressVo = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        fareVo.setAddress(memberAddressVo);
        if (memberAddressVo != null) {
            String phone = memberAddressVo.getPhone();
            BigDecimal fare = new BigDecimal(String.valueOf(phone.charAt(phone.length() - 1)));
            fareVo.setFare(fare);
            return fareVo;
        }
        return null;
    }


}
