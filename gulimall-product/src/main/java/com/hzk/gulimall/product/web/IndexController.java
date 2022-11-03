package com.hzk.gulimall.product.web;


import com.hzk.gulimall.product.entity.CategoryEntity;
import com.hzk.gulimall.product.service.CategoryService;
import com.hzk.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kee
 * @version 1.0
 * @date 2022/10/8 15:33
 */
@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @GetMapping(value = {"/", "index.html"})
    private String indexPage(Model model) {

        //1、查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();
        model.addAttribute("categories", categoryEntities);
        return "index";
    }

    @GetMapping(value = "/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catelog2Vo>> getCatalogJson() {

        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;

    }
    @GetMapping("/hello")
    @ResponseBody
    private String hello() {
        //先得到锁
        RLock lock = redissonClient.getLock("lock");

        //加锁
        lock.lock();

        //业务操作
        try {
            lock.lock(10,TimeUnit.SECONDS);
            System.out.println("加锁成功" + Thread.currentThread());
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {

        }finally {
            //释放锁
            lock.unlock();
            System.out.println("释放锁.." + Thread.currentThread());
        }

        return "hello";
    }
}
