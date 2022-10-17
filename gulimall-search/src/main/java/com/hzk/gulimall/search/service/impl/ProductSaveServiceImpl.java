package com.hzk.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hzk.common.es.SkuEsModel;
import com.hzk.gulimall.search.config.GulimallElasticSearchConfig;
import com.hzk.gulimall.search.constant.EsConstant;
import com.hzk.gulimall.search.service.ProductSavaService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/30 15:41
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSavaService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> models) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel model : models) {
            IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(model.getSkuId().toString());
            String str = JSONObject.toJSONString(model);
            request.source(str, XContentType.JSON);
            bulkRequest.add(request);
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //TODO 批量处理错误
        boolean b = bulk.hasFailures();
        List<String> list = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架成功: {}", list);
        return b;
    }
}
