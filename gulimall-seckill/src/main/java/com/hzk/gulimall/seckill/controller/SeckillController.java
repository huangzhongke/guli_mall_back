package com.hzk.gulimall.seckill.controller;

import com.hzk.common.utils.R;
import com.hzk.gulimall.seckill.service.SeckillService;
import com.hzk.gulimall.seckill.to.SecKillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/3 17:03
 */
@Controller
public class SeckillController {
    @Autowired
    SeckillService seckillService;

    /**
     * @return 返回当前参与秒杀的商品
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        List<SecKillSkuRedisTo> list = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(list);
    }
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R getSkuSeckillInfo(@PathVariable("skuId") Long skuId){
        SecKillSkuRedisTo to = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId,
                       @RequestParam("key") String key,
                       @RequestParam("num") Integer num, Model model){
        String orderSn = seckillService.kill(killId,key,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }
}
