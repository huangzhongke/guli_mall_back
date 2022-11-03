package com.hzk.gulimall.search.controller;

import com.hzk.gulimall.search.service.MallSearchService;
import com.hzk.gulimall.search.vo.SearchParam;
import com.hzk.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author kee
 * @version 1.0
 * @date 2022/10/21 10:53
 */

@Controller
public class SearchController {
    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParam searchParam, Model model) {
         SearchResult result =  mallSearchService.search(searchParam);
        model.addAttribute("result",result);
        return "list";
    }
}
