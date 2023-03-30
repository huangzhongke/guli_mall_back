package com.hzk.gulimall.order.web;

import com.hzk.gulimall.order.service.OrderService;
import com.hzk.gulimall.order.vo.OrderConfirmVo;
import com.hzk.gulimall.order.vo.OrderSubmitVo;
import com.hzk.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 15:21
 */
@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.orderConfirm();

        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submit(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {

        SubmitOrderResponseVo submitOrderResponseVo = null;
        try {
            submitOrderResponseVo = orderService.submitOrder(vo);
            if (submitOrderResponseVo.getCode() == 0){
                //如果成功
                model.addAttribute("submitOrderResponse",submitOrderResponseVo);
                return "pay";
            }
            else {
                String msg = "下单失败：";
                switch (submitOrderResponseVo.getCode()){
                    case 1: msg += "订单信息过期，请刷新页面重新提交"; break;
                    case 2: msg += "订单商品价格发生变化，请确认后重新提交";break;
                    case 3: msg += "订单商品库存发生变化，请确认后重新提交";break;
                }
                redirectAttributes.addFlashAttribute("msg",msg);
                //失败
                return "redirect:http://order.gulimall.com/toTrade";
            }
        } catch (Exception e) {
            String msg = "下单失败";
            redirectAttributes.addFlashAttribute("msg",msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }

    }

    @GetMapping("/orderList.html")
    public String orderList(Model model,RedirectAttributes redirectAttributes){

        return "list.html";
    }
}
