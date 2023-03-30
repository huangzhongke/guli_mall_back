package com.hzk.gulimall.member.web;

import com.alibaba.fastjson.JSONObject;
import com.hzk.common.utils.R;
import com.hzk.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/24 15:44
 */
@Controller
public class MemberWebController {
    @Autowired
    OrderFeignService orderFeignService;
    @GetMapping("/memberOrder.html")
    public String memberOrderPage(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                  Model model){
        Map<String, Object> params = new HashMap<>();
        params.put("page",String.valueOf(pageNum));
        R r = orderFeignService.listWithItem(params);
        System.out.println(JSONObject.toJSONString(r));
        model.addAttribute("orders",r);
        return "orderList.html";
    }
}
