package com.hzk.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.hzk.gulimall.order.config.AlipayTemplate;
import com.hzk.gulimall.order.service.OrderService;
import com.hzk.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/24 10:01
 */
@Controller
public class PayWebController {
    @Autowired
    AlipayTemplate alipayTemplate;
    @Autowired
    OrderService orderService;
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String pay(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getPayVoByOrderSn(orderSn);
        String pay = alipayTemplate.pay(payVo);
        return pay;
    }
}
