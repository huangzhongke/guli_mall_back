package com.hzk.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.common.exception.NoStockException;
import com.hzk.common.to.OrderTo;
import com.hzk.common.to.SkuHasStockVo;
import com.hzk.common.to.mq.StockDetailTo;
import com.hzk.common.to.mq.StockLockedTo;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.common.utils.R;
import com.hzk.gulimall.ware.constant.LockStatus;
import com.hzk.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.hzk.gulimall.ware.dao.WareSkuDao;
import com.hzk.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.hzk.gulimall.ware.entity.WareOrderTaskEntity;
import com.hzk.gulimall.ware.entity.WareSkuEntity;
import com.hzk.gulimall.ware.feign.OrderFeignService;
import com.hzk.gulimall.ware.feign.ProductFeignService;
import com.hzk.gulimall.ware.service.WareOrderTaskDetailService;
import com.hzk.gulimall.ware.service.WareOrderTaskService;
import com.hzk.gulimall.ware.service.WareSkuService;
import com.hzk.gulimall.ware.vo.OrderItemVo;
import com.hzk.gulimall.ware.vo.OrderVo;
import com.hzk.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareOrderTaskDetailDao wareOrderTaskDetailDao;
    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    WareOrderTaskService wareOrderTaskService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderFeignService orderFeignService;

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

    @Override
    public void unlockStock(StockLockedTo stockLockedTo) {
        /**
         * 解锁
         * 1、查询数据库关于这个订单锁定库存信息
         *   有：证明库存锁定成功了
         *      解锁：订单状况
         *          1、没有这个订单，必须解锁库存
         *          2、有这个订单，不一定解锁库存
         *              订单状态：已取消：解锁库存
         *                      已支付：不能解锁库存
         */
        Long taskDetailId = stockLockedTo.getId();
        WareOrderTaskDetailEntity taskDetailEntity = wareOrderTaskDetailService.getById(taskDetailId);
        if (taskDetailEntity != null) {
            //解锁
            Long taskId = taskDetailEntity.getTaskId();
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(taskId);
            String orderSn = taskEntity.getOrderSn();
            R r = orderFeignService.getOrderStatus(orderSn);
            if (r.getCode() != 0) {
                throw new RuntimeException("远程服务调用失败");
            }
            //远程调用成功
            OrderVo orderVo = r.getData(new TypeReference<OrderVo>() {
            });
            if (orderVo == null || orderVo.getStatus() == 4) {
                if (taskDetailEntity.getLockStatus() == LockStatus.LOCKED) {
                    //回滚库存
                    unLockStock(taskDetailEntity.getSkuId(), taskDetailEntity.getWareId(), taskDetailEntity.getSkuNum(), taskDetailId);
                }
            }
        }
    }
    @Transactional
    public void unLockStock(Long skuId, Long wareId, Integer num, Long detailId) {
        wareSkuDao.unLockStock(skuId, wareId, num);
        WareOrderTaskDetailEntity entity = new WareOrderTaskDetailEntity();
        entity.setId(detailId);
        entity.setLockStatus(LockStatus.RELEASED);
        wareOrderTaskDetailService.updateById(entity);

    }

    @Transactional
    @Override
    public Boolean orderStockLock(WareSkuLockVo vo) {
        /**
         * 追溯订单
         */
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(taskEntity);
        //锁定库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream().map(item -> {
            SkuWareHasStock wareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            wareHasStock.setSkuId(skuId);
            wareHasStock.setNum(item.getCount());
            List<Long> wareIds = wareSkuDao.listWareHasSkuStock(skuId);
            wareHasStock.setWareIds(wareIds);
            return wareHasStock;
        }).collect(Collectors.toList());
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {
            Boolean stockFlag = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareIds();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                Long count = wareSkuDao.lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if (count == 1L) {
                    //表示锁定成功
                    stockFlag = true;
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity(null, skuId, "", skuWareHasStock.num, taskEntity.getId(), wareId, 1);
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskDetailEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(taskDetailEntity, stockDetailTo);
                    stockLockedTo.setDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend(RabbitMqConstant.STOCK_EVENT_EXCHANGE, RabbitMqConstant.STOCK_LOCKED_ROUTING_KEY, stockLockedTo);
                    break;
                }
            }
            if (!stockFlag) {
                //如果没锁住
                throw new NoStockException(skuId);
            }
        }
        return true;
    }

    /**
     * 当订单服务网络延迟 或者卡顿,造成库存解锁消息接收到发现订单状态还是已创建状态，那么这时候库存将永远不会解锁
     * 所以当订单关闭之后要在发一条消息给库存解锁服务让库存进行解锁
     * @param orderTo
     */
    @Override
    public void unlockStock(OrderTo orderTo) {
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);

        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().lambda()
                .eq(WareOrderTaskDetailEntity::getTaskId, wareOrderTaskEntity.getId())
                .eq(WareOrderTaskDetailEntity::getLockStatus, LockStatus.LOCKED));
        for (WareOrderTaskDetailEntity entity : list) {
            unLockStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum(),entity.getId());
        }


    }
    @Transactional
    @Override
    public void orderStockMinus(String orderSn) {
        WareOrderTaskEntity orderTask = wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().lambda()
                .eq(WareOrderTaskDetailEntity::getTaskId, orderTask.getId())
                .eq(WareOrderTaskDetailEntity::getLockStatus, LockStatus.LOCKED));
        for (WareOrderTaskDetailEntity entity : list) {
            //修改taskDetail的lock_status状态置为3
            WareOrderTaskDetailEntity detail = new WareOrderTaskDetailEntity();
            detail.setId(entity.getId());
            detail.setLockStatus(LockStatus.FINISH);
            wareOrderTaskDetailService.updateById(detail);
            wareSkuDao.minusStock(entity.getSkuId(),entity.getSkuNum(),entity.getWareId());
        }
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }
}