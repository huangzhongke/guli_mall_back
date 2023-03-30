package com.hzk.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.common.exception.NoStockException;
import com.hzk.common.to.OrderTo;
import com.hzk.common.to.SkuHasStockVo;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.common.utils.R;
import com.hzk.common.vo.MemberResponseVo;
import com.hzk.gulimall.order.constant.OrderConstant;
import com.hzk.gulimall.order.constant.PayConstant;
import com.hzk.gulimall.order.dao.OrderDao;
import com.hzk.gulimall.order.entity.OrderEntity;
import com.hzk.gulimall.order.entity.OrderItemEntity;
import com.hzk.gulimall.order.entity.PaymentInfoEntity;
import com.hzk.gulimall.order.enume.OrderStatusEnum;
import com.hzk.gulimall.order.feign.CartFeignService;
import com.hzk.gulimall.order.feign.MemberFeignService;
import com.hzk.gulimall.order.feign.ProductFeignService;
import com.hzk.gulimall.order.feign.WmsFeignService;
import com.hzk.gulimall.order.interceptor.LoginUserInterceptor;
import com.hzk.gulimall.order.service.OrderItemService;
import com.hzk.gulimall.order.service.OrderService;
import com.hzk.gulimall.order.service.PaymentInfoService;
import com.hzk.gulimall.order.to.OrderCreateTo;
import com.hzk.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();
    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PaymentInfoService paymentInfoService;
    //public void a(){
    //   OrderServiceImpl orderServiceiml = (OrderServiceImpl) AopContext.currentProxy();
    //
    //}
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单确认页的信息
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public OrderConfirmVo orderConfirm() throws ExecutionException, InterruptedException {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.threadLocal.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //  List<MemberAddressVo> address;
        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            orderConfirmVo.setAddress(address);
        }, executor);

        //  List<OrderItemVo> items;
        CompletableFuture<Void> getCartItemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(currentUserCartItems);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> skuIds = items.stream().map((item) -> {
                return item.getSkuId();
            }).collect(Collectors.toList());
            R r = wmsFeignService.getSkuHasStock(skuIds);
            if (r != null) {
                List<SkuHasStockVo> skuStockVos = r.getData("data", new TypeReference<List<SkuHasStockVo>>() {
                });
                if (skuStockVos != null) {
                    Map<Long, Boolean> collect = skuStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                    orderConfirmVo.setStocks(collect);
                }
            }


        }, executor);

        //    Integer integration;//优惠卷信息
        Integer integration = memberResponseVo.getIntegration();
        orderConfirmVo.setIntegration(integration);
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);
        CompletableFuture.allOf(getAddressFuture, getCartItemsFuture).get();
        return orderConfirmVo;
    }

    /**
     * 提交订单
     * @param orderSubmitVo
     * @return 提交结果
     */
    //@GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        //获取当前进行操作的用户
        orderSubmitVoThreadLocal.set(orderSubmitVo);
        MemberResponseVo memberResponseVo = LoginUserInterceptor.threadLocal.get();

        //校验OrderToken
        //if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end
        //如果比对成功则删除 返回1 ,比对失败返回0 原子性操作
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = orderSubmitVo.getOrderToken();
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        if (execute == 0L) {
            responseVo.setCode(1);
            return responseVo;
            //失败返回
        }
        else {
            //创建订单
            OrderCreateTo orderCreateTo = orderCreate();
            //验价
            BigDecimal payAmount = orderCreateTo.getOrder().getPayAmount();
            BigDecimal payPrice = orderSubmitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                //验价成功
                saveOrder(orderCreateTo);
                //锁定库存
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVoList = orderCreateTo.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemVoList);
                R r = wmsFeignService.orderStockLock(wareSkuLockVo);

                if (r.getCode()==0){
                    //锁定成功
                    responseVo.setOrder(orderCreateTo.getOrder());
                    responseVo.setCode(0);
                    rabbitTemplate.convertAndSend(RabbitMqConstant.ORDER_EVENT_EXCHANGE,RabbitMqConstant.ORDER_CREATE_ORDER_ROUTING_KEY,orderCreateTo.getOrder());
                    //int i = 10/0;
                    return responseVo;
                }else {
                    responseVo.setCode(3);
                    String msg = r.get("msg").toString();
                    throw new NoStockException(msg);
                }

            }
            else {
                responseVo.setCode(2);
                return responseVo;
            }

        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {

        return baseMapper.selectOne(new QueryWrapper<OrderEntity>().lambda().eq(OrderEntity::getOrderSn,orderSn));
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        //先查一遍
        OrderEntity orderEntity = this.getById(entity.getId());
        if (orderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()){
            OrderEntity entity1 = new OrderEntity();
            entity1.setId(orderEntity.getId());
            entity1.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(entity1);
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity,orderTo);
            rabbitTemplate.convertAndSend(
                    RabbitMqConstant.ORDER_EVENT_EXCHANGE,
                    RabbitMqConstant.ORDER_RELEASE_OTHER_ROUTING_KEY,
                    orderTo);
        }
    }

    @Override
    public PayVo getPayVoByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        if (orderEntity == null){
            return null;
        }
        PayVo payVo = new PayVo();
        BigDecimal payAmount = orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP);
        List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().lambda().eq(OrderItemEntity::getOrderSn, orderSn));
        OrderItemEntity orderItemEntity = list.get(0);
        payVo.setTotal_amount(payAmount.toString());
        payVo.setOut_trade_no(orderEntity.getOrderSn());
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberResponseVo memberResponseVo = LoginUserInterceptor.threadLocal.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().lambda()
                        .eq(OrderEntity::getMemberId,memberResponseVo.getId())
                        .orderByDesc(OrderEntity::getId,OrderEntity::getId)
        );
        List<OrderEntity> collect = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().lambda().eq(OrderItemEntity::getOrderSn, order.getOrderSn()));
            order.setOrderItemEntityList(list);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(collect);

        return new PageUtils(page);
    }

    @Override
    public String asyncNotify(String notifyData) {
        return null;
    }
    /**
     * 处理支付宝的支付结果
     * @param asyncVo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String handlePayResult(PayAsyncVo asyncVo) {
        //保存交易流水信息
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setOrderSn(asyncVo.getOut_trade_no());
        paymentInfo.setAlipayTradeNo(asyncVo.getTrade_no());
        paymentInfo.setTotalAmount(new BigDecimal(asyncVo.getBuyer_pay_amount()));
        paymentInfo.setSubject(asyncVo.getBody());
        paymentInfo.setPaymentStatus(asyncVo.getTrade_status());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setCallbackTime(asyncVo.getNotify_time());
        //添加到数据库中
        paymentInfoService.save(paymentInfo);

        //修改订单状态
        //获取当前状态
        String tradeStatus = asyncVo.getTrade_status();

        if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
            //支付成功状态
            String orderSn = asyncVo.getOut_trade_no(); //获取订单号
            this.baseMapper.updateOrderStatus(orderSn,OrderStatusEnum.PAYED.getCode(), PayConstant.ALIPAY);
            //TODO 远程调用库存系统扣减库存
            wmsFeignService.orderStockMinus(orderSn);
        }


        return "success";
    }


    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        orderService.save(order);
        orderItemService.saveBatch(orderCreateTo.getOrderItems());

    }

    private OrderCreateTo orderCreate() {

        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity entity = buildOrder(orderSn);
        orderCreateTo.setOrder(entity);

        //购物项
        List<OrderItemEntity> orderItemEntityList = buildOrderItems(entity.getOrderSn());
        orderCreateTo.setOrderItems(orderItemEntityList);

        //计算订单价格
        computePrice(entity, orderItemEntityList);
        return orderCreateTo;
    }

    /**
     * 计算优惠后的价格
     *
     * @param entity              订单
     * @param orderItemEntityList 订单项
     */
    private void computePrice(OrderEntity entity, List<OrderItemEntity> orderItemEntityList) {
        BigDecimal totalPrice = new BigDecimal("0.0");
        BigDecimal couponPrice = new BigDecimal("0.0");
        BigDecimal integrationPrice = new BigDecimal("0.0");
        BigDecimal promotionPrice = new BigDecimal("0.0");
        BigDecimal giftGrowth = new BigDecimal("0.0");
        BigDecimal giftIntegration = new BigDecimal("0.0");
        for (OrderItemEntity orderItemEntity : orderItemEntityList) {
            couponPrice = couponPrice.add(orderItemEntity.getCouponAmount());
            integrationPrice = integrationPrice.add(orderItemEntity.getIntegrationAmount());
            promotionPrice = promotionPrice.add(orderItemEntity.getPromotionAmount());
            totalPrice = totalPrice.add(orderItemEntity.getRealAmount());
            giftGrowth = giftGrowth.add(new BigDecimal(orderItemEntity.getGiftGrowth()));
            giftIntegration = giftIntegration.add(new BigDecimal(orderItemEntity.getGiftGrowth()));
        }
        //优惠价格+积分
        entity.setTotalAmount(totalPrice);
        entity.setPayAmount(entity.getTotalAmount().add(entity.getFreightAmount()));
        entity.setPromotionAmount(promotionPrice);
        entity.setIntegrationAmount(integrationPrice);
        entity.setCouponAmount(couponPrice);
        entity.setGrowth(giftGrowth.intValue());
        entity.setIntegration(giftIntegration.intValue());

        //订单状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        entity.setDeleteStatus(0);

    }

    /**
     * 创建订单
     *
     * @param orderSn 唯一订单号标识
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderSubmitVo orderSubmitVo = orderSubmitVoThreadLocal.get();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.threadLocal.get();
        OrderEntity entity = new OrderEntity();
        entity.setMemberId(memberResponseVo.getId());
        entity.setMemberUsername(memberResponseVo.getUsername());
        entity.setOrderSn(orderSn);

        //运费

        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {
        });
        entity.setFreightAmount(fareVo.getFare());
        MemberAddressVo address = fareVo.getAddress();
        //收货地址
        entity.setReceiverCity(address.getCity());
        entity.setReceiverName(address.getName());
        entity.setReceiverPostCode(address.getPostCode());
        entity.setReceiverDetailAddress(address.getDetailAddress());
        entity.setReceiverRegion(address.getRegion());
        entity.setReceiverPhone(address.getPhone());
        entity.setReceiverProvince(address.getProvince());
        return entity;
    }

    /**
     * 订单项
     *
     * @param orderSn 订单号
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map((cartItem) -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setSkuName(cartItem.getTitle());
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 单个购物商品项
     *
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();


        //sku信息
        Long skuId = cartItem.getSkuId();
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";"));
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        //积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        //spu信息
        R r = productFeignService.getSpuBySkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        //购物项金额
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        BigDecimal originPrice = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        BigDecimal finalPrice = originPrice
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getIntegrationAmount())
                .subtract(orderItemEntity.getCouponAmount());
        orderItemEntity.setRealAmount(finalPrice);
        return orderItemEntity;
    }
}