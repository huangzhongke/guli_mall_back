package com.hzk.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hzk.common.constant.CartConstant;
import com.hzk.common.utils.R;
import com.hzk.gulimall.cart.feign.ProductFeignService;
import com.hzk.gulimall.cart.interceptor.CartInterceptor;
import com.hzk.gulimall.cart.service.CartService;
import com.hzk.gulimall.cart.vo.CartItemVo;
import com.hzk.gulimall.cart.vo.CartVo;
import com.hzk.gulimall.cart.vo.SkuInfoVo;
import com.hzk.gulimall.cart.vo.UserInfoTo;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author kee
 * @version 1.0
 * @date 2023/1/30 17:00
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    /**
     * @param skuId 商品id
     * @param num   商品数量
     * @return 封装好的数据
     */
    @Override
    public CartItemVo addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        String result = (String) cartOps.get(skuId.toString());
        // 当购物车中没有选中的商品则添加商品
        if (StringUtils.isEmpty(result)) {
            // 异步编排
            CartItemVo cartItemVo = new CartItemVo();
            CompletableFuture<Void> getSkuInfo = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfo(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setCheck(true);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setCount(num);
                cartItemVo.setTitle(skuInfo.getSkuTitle());
                cartItemVo.setPrice(skuInfo.getPrice());
            }, executor);
            //远程调用
            CompletableFuture<Void> getSaleAttrValusListString = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttrValues = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(skuSaleAttrValues);
            }, executor);
            try {
                CompletableFuture.allOf(getSkuInfo, getSaleAttrValusListString).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 存到redis当中
            cartOps.put(skuId.toString(), JSONObject.toJSONString(cartItemVo));
            return cartItemVo;
        } else {
            //有则需要修改数据
            CartItemVo cartVo = JSONObject.parseObject(result, CartItemVo.class);
            cartVo.setCount(cartVo.getCount() + num);
            //同时redis当中也需要修改
            cartOps.put(skuId.toString(), JSONObject.toJSONString(cartVo));

            return cartVo;
        }

    }

    /**
     * @param skuId 商品Id
     * @return 购物车中的购物项
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String result = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSONObject.parseObject(result, CartItemVo.class);
        return cartItemVo;
    }

    /**
     * @return 购物车数据
     */
    @Override
    public CartVo getCart() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        CartVo cartVo = new CartVo();
        if (userInfoTo.getUserId() != null) {
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            //合并临时购物车
            List<CartItemVo> tempCartItems = getCartItems(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
            if (tempCartItems != null && tempCartItems.size() > 0) {
                for (CartItemVo cartItem : tempCartItems) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
                clearCart(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
            }
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
            return cartVo;

        } else {
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);

        }
        return cartVo;
    }

    /**
     * 删除购物车数据
     *
     * @param cartKey 临时用户user-key或者用户token
     */
    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    /**
     * 修改购物车中商品是否选中
     *
     * @param skuId
     * @param check
     */
    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1 ? true : false);
        cartOps.put(skuId.toString(), JSONObject.toJSONString(cartItem));
    }

    @Override
    public void changeCountItem(Long skuId, Integer count) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(count);
        cartOps.put(skuId.toString(), JSONObject.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCurrentUserCartItems() {
        ThreadLocal<UserInfoTo> threadLocal = CartInterceptor.threadLocal;
        UserInfoTo userInfoTo = threadLocal.get();
        if (userInfoTo.getUserId() == null){
            return null;
        }else {
            String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> cartItems = getCartItems(cartKey).stream()
                    .filter(item->item.getCheck()).map(item -> {
                //由于考虑到时间变化 购物车中存放的商品价格也会发生变化需要远程调用更新价格
                BigDecimal price = productFeignService.getPrice(item.getSkuId());
                item.setPrice(price);
                return item;
            }).collect(Collectors.toList());
            return cartItems;

        }
    }

    @Override
    public void deleteCartItems(List<Long> skuIds) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        for (Long skuId : skuIds) {
            cartOps.delete(skuId.toString());
        }
    }

    /**
     * 根据临时用户的user-key 或者 用户的token获取对应的购物车数据
     *
     * @param cartKey
     * @return 购物车数据
     */
    public List<CartItemVo> getCartItems(String cartKey) {

        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(cartKey);
        List<Object> values = ops.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> collect = values.stream().map((obj) -> {
                String result = obj.toString();
                CartItemVo cartItemVo = JSONObject.parseObject(result, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @NotNull
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            //如果用户已经登录
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
    