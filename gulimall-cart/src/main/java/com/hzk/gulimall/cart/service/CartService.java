package com.hzk.gulimall.cart.service;

import com.hzk.gulimall.cart.vo.CartItemVo;
import com.hzk.gulimall.cart.vo.CartVo;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/1/30 16:06
 */

public interface CartService {
    /**
     * 添加购入车项
     *
     * @param skuId
     * @param num
     * @return
     */
    CartItemVo addToCart(Long skuId, Integer num);

    /**
     * 获取购物车项
     *
     * @param skuId
     * @return
     */
    CartItemVo getCartItem(Long skuId);

    CartVo getCart();

    void clearCart(String cartKey);

    /**
     * 修改购物车商品是否选中
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改商品数量
     * @param skuId 商品id
     * @param count 商品数量
     */
    void changeCountItem(Long skuId, Integer count);

    void deleteItem(Long skuId);

    List<CartItemVo> getCurrentUserCartItems();

    void deleteCartItems(List<Long> skuIds);
}
