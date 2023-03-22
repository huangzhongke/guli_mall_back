package com.hzk.gulimall.search.service;

import com.hzk.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/30 15:41
 */

public interface ProductSavaService {
    boolean productStatusUp(List<SkuEsModel> models) throws IOException;
}
