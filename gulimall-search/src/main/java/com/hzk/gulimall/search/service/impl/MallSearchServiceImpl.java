package com.hzk.gulimall.search.service.impl;

import com.hzk.gulimall.search.config.GulimallElasticSearchConfig;
import com.hzk.gulimall.search.constant.EsConstant;
import com.hzk.gulimall.search.service.MallSearchService;
import com.hzk.gulimall.search.vo.SearchParam;
import com.hzk.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author kee
 * @version 1.0
 * @date 2022/10/21 14:26
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult result = null;
        // 第一步封装请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            // 第二步 对请求结果做一个数据封装
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //第三步返回
        return result;
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /**
         *  模糊匹配 过滤(catalogId ,brandId, (attrs.attrId,attrs.attrValue),hasStock,skuPrice)
         */

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));

        }
        //三级分类id
        if (searchParam.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 品牌id
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        //属性
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {

            for (String attrStr : searchParam.getAttrs()) {
                //1_5寸:8寸
                BoolQueryBuilder boolQueryAttrs = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQueryAttrs.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQueryAttrs.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", boolQueryAttrs, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }


        // 库存
        boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));

        // 价格
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] skuPriceArr = searchParam.getSkuPrice().split("_");
            if (skuPriceArr.length == 2) {
                if (skuPriceArr[0].equalsIgnoreCase("")) {
                    rangeQuery.lt(skuPriceArr[1]);
                } else {
                    rangeQuery.gt(skuPriceArr[0]).lt(skuPriceArr[1]);
                }
            } else if (skuPriceArr.length == 1) {
                rangeQuery.gt(skuPriceArr[0]);
            }
            boolQuery.filter(rangeQuery);
        }

        searchSourceBuilder.query(boolQuery);
        /**
         * 分页 排序 高亮
         */

        //排序 sort = skuPrice_asc/desc
        //
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            String field = s[0];
            String[] s2 = s[1].split("/");
            SortOrder sortOrder = s2[0].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(field, sortOrder);
        }


        //分页
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuTitle");
        highlightBuilder.preTags("<b style='color:red'>");
        highlightBuilder.postTags("</b>");
        searchSourceBuilder.highlighter(highlightBuilder);

        /**
         * 聚合
         */
        //聚合
        //TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brandId").size(10);
        //brand_agg.subAggregation(AggregationBuilders.terms("brandName").size(10));
        //brand_agg.subAggregation(AggregationBuilders.terms("brandImg").size(10));
        //
        //TermsAggregationBuilder catelog_agg = AggregationBuilders.terms("catalogId").size(10);
        //catelog_agg.subAggregation(AggregationBuilders.terms("catalogName").size(10));
        //
        //NestedAggregationBuilder nestedAggregation = AggregationBuilders.nested(null, "attrs");
        //
        //searchSourceBuilder.aggregation(brand_agg);
        //searchSourceBuilder.aggregation(catelog_agg);

        SearchRequest request = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        System.out.println("构建的DSL语句：" + searchSourceBuilder);
        return request;
    }

    private SearchResult buildSearchResult(SearchResponse response) {
        return null;
    }


}
