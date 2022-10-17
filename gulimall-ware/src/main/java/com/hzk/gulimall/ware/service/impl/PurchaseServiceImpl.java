package com.hzk.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.constant.WareConstant;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.ware.dao.PurchaseDao;
import com.hzk.gulimall.ware.entity.PurchaseDetailEntity;
import com.hzk.gulimall.ware.entity.PurchaseEntity;
import com.hzk.gulimall.ware.service.PurchaseDetailService;
import com.hzk.gulimall.ware.service.PurchaseService;
import com.hzk.gulimall.ware.service.WareSkuService;
import com.hzk.gulimall.ware.vo.MergeVo;
import com.hzk.gulimall.ware.vo.PurchaseDoneItemVo;
import com.hzk.gulimall.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;
    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().lambda().eq(PurchaseEntity::getStatus, 0).or().eq(PurchaseEntity::getStatus, 1)
        );

        return new PageUtils(page);
    }
    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        //TODO 只有新建状态和已分配状态才能进行之后操作 只有 0和1才能进行合并操作
        List<Long> items = mergeVo.getItems();
        List<PurchaseDetailEntity> purchaseList = items.stream().map(item -> {
            PurchaseDetailEntity byId = purchaseDetailService.getById(item);
            return byId;
        }).filter(entity -> {
            if (entity.getStatus() == WareConstant.PurchaseDetailEnum.CREATED.getCode() || entity.getStatus() == WareConstant.PurchaseDetailEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        if (purchaseList.size() == 0){
           throw new RuntimeException("只有新建状态或已分配状态的采购项才能进行合并");
        }
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null ) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }



        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> list = items.stream().map(id -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.ASSIGNED.getCode());
            purchaseDetailEntity.setId(id);
            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(list);

        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setId(finalPurchaseId);
        purchase.setUpdateTime(new Date());
        this.updateById(purchase);

    }
    @Transactional
    @Override
    public void received(List<Long> ids) {
        //先查所有已分配的采购单 并且状态只能为0或1
        List<PurchaseEntity> purchaseEntityList = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            //只能领取刚创建 或者 已经已分配的采购单
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getCode() || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(entity -> {
            //分配完设置为已领取
            entity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getCode());
            entity.setUpdateTime(new Date());
            return entity;
        }).collect(Collectors.toList());
        this.updateBatchById(purchaseEntityList);

        //采购单设置成了已领取，那么采购项对应的状态需要设置为正在采购，那么下次合并采购单的时候就这些已经在采购的采购项就不能合并了
        purchaseEntityList.forEach(item -> {
            // 根据采购单id 查询到对应的采购项
            List<PurchaseDetailEntity> purchaseDetailEntityList = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect = purchaseDetailEntityList.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);
        });
        //只能领取分配给自己的采购单

    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo vo) {
        // 先修改采购项, 如果采购项有一项失败，那么采购单的状态就是有异常4
        Long purchaseId = vo.getId();

        Boolean flag = true;
        List<PurchaseDetailEntity> details = new ArrayList<>();
        for (PurchaseDoneItemVo item : vo.getItems()) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }else {
                purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailEnum.FINISH.getCode());
                PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());

                wareSkuService.addStock(byId.getSkuId(),byId.getWareId(),byId.getSkuNum());
            }
            purchaseDetailEntity.setStatus(item.getStatus());
            purchaseDetailEntity.setId(item.getItemId());
            details.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(details);
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        purchaseEntity.setStatus(flag? WareConstant.PurchaseDetailEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);
    }

}