package com.hzk.gulimall.search.service;

import com.hzk.gulimall.search.vo.SearchParam;
import com.hzk.gulimall.search.vo.SearchResult;

/**
 * @author kee
 * @version 1.0
 * @date 2022/10/21 14:26
 */
public interface MallSearchService {
    SearchResult search(SearchParam searchParam);

}
