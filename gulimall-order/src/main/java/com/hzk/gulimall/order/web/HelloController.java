package com.hzk.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 10:02
 */
@Controller
public class HelloController {


    @GetMapping("/{page}.html")
    public String hello(@PathVariable("page") String page) {
        return page;
    }
}
