package com.hzk.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.ware.dao.PurchaseDetailDao;
import com.hzk.gulimall.ware.entity.PurchaseDetailEntity;
import com.hzk.gulimall.ware.service.PurchaseDetailService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * key:
         * status:
         * wareId:
         */
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new QueryWrapper<PurchaseDetailEntity>().lambda();

        if (null!=params.get("key") && !StringUtils.isEmpty(params.get("key").toString())) {
            String key = params.get("key").toString();
            wrapper.and(w -> {
                w.eq(PurchaseDetailEntity::getPurchaseId,key)
                       .eq(PurchaseDetailEntity::getSkuId,key);
            });
        }
        if (null !=  params.get("status") &&!StringUtils.isEmpty(params.get("status").toString())){
            String status = params.get("status").toString();
            wrapper.eq(PurchaseDetailEntity::getStatus,status);
        }
        if (null!=params.get("wareId") && !StringUtils.isEmpty(params.get("wareId").toString())){
            String wareId = params.get("wareId").toString();
            wrapper.eq(PurchaseDetailEntity::getWareId,wareId);
        }
        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper

        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id) {
        List<PurchaseDetailEntity> list = this.list(new QueryWrapper<PurchaseDetailEntity>().lambda().eq(PurchaseDetailEntity::getPurchaseId, id));

        return list;
    }

}