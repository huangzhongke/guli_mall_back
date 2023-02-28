package com.hzk.gulimall.cart.controller;

import com.hzk.gulimall.cart.service.CartService;
import com.hzk.gulimall.cart.vo.CartItemVo;
import com.hzk.gulimall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/2 15:44
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){

        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("count") Integer count){

        cartService.changeCountItem(skuId,count);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,
                            @RequestParam("check") Integer check){

        cartService.checkItem(skuId,check);
        return "redirect:http://cart.gulimall.com/cart.html";
    }
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {

        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart",cartVo);

        //userKey != null 临时用户
        return "cartList";

    }

    @GetMapping("addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra) {
        cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }


    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVo cartItemVo = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItemVo);
        return "success";
    }
}
