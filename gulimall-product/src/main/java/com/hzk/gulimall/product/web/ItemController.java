package com.hzk.gulimall.product.web;

import com.hzk.gulimall.product.service.SkuInfoService;
import com.hzk.gulimall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2022/11/21 11:25
 */
@Controller
public class ItemController {
    @Autowired
    SkuInfoService service;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model){
        SkuItemVo vo = service.item(skuId);
        model.addAttribute("item",vo);
        return "item";
    }
}
