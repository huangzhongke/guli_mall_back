package com.hzk.gulimall.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hzk.common.es.SkuEsModel;
import com.hzk.common.utils.R;
import com.hzk.gulimall.search.config.GulimallElasticSearchConfig;
import com.hzk.gulimall.search.constant.EsConstant;
import com.hzk.gulimall.search.feign.ProductFeignService;
import com.hzk.gulimall.search.service.MallSearchService;
import com.hzk.gulimall.search.vo.AttrResponseVo;
import com.hzk.gulimall.search.vo.BrandResponseVo;
import com.hzk.gulimall.search.vo.SearchParam;
import com.hzk.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kee
 * @version 1.0
 * @date 2022/10/21 14:26
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    RestHighLevelClient client;
    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult result = null;
        // ?????????????????????
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            //????????? ????????????????????????
            SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
            // ????????? ????????????????????????????????????
            result = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //???????????????
        return result;
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        /**
         *  ???????????? ??????(catalogId ,brandId, (attrs.attrId,attrs.attrValue),hasStock,skuPrice)
         */

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));

        }
        //????????????id
        if (searchParam.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // ??????id
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        //??????
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {

            for (String attrStr : searchParam.getAttrs()) {
                //1_5???:8???
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

        if (searchParam.getHasStock() != null) {
            // ??????
            boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));

        }

        // ??????
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
         * ?????? ?????? ??????
         */

        //?????? sort = skuPrice_asc/desc
        //
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] s = sort.split("_");
            String field = s[0];
            String[] s2 = s[1].split("/");
            SortOrder sortOrder = s2[0].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(field, sortOrder);
        }


        //??????
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //??????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("skuTitle");
        highlightBuilder.preTags("<b style='color:red'>");
        highlightBuilder.postTags("</b>");
        searchSourceBuilder.highlighter(highlightBuilder);

        /**
         * ??????
         */
        //??????

        //TODO ??????????????????
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        //TODO ??????????????????
        TermsAggregationBuilder catelogAgg = AggregationBuilders.terms("catelog_agg").field("catalogId").size(20);
        catelogAgg.subAggregation(AggregationBuilders.terms("catelog_name_agg").field("catalogName").size(1));

        //TODO ??????????????????
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAggs = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attrIdAggs.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAggs.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAggs);

        //TODO ????????????DSL???????????????
        searchSourceBuilder.aggregation(brandAgg);
        searchSourceBuilder.aggregation(catelogAgg);
        searchSourceBuilder.aggregation(attrAgg);

        SearchRequest request = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
        System.out.println("?????????DSL?????????" + searchSourceBuilder);
        return request;
    }

    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {

        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit hit : hits1) {
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSONObject.parseObject(sourceAsString, SkuEsModel.class);
            if (!StringUtils.isEmpty(param.getKeyword())) {
                HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                String title = skuTitle.getFragments()[0].toString();
                skuEsModel.setSkuTitle(title);
            }
            skuEsModelList.add(skuEsModel);
        }
        result.setProduct(skuEsModelList);
        //????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrId = bucket.getKeyAsNumber().longValue();

            ParsedStringTerms attr_name_aggs = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_aggs.getBuckets().get(0).getKeyAsString();

            List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                return item.getKeyAsString();
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValue);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
        // ????????????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<? extends Terms.Bucket> brandBuckets = brand_agg.getBuckets();
        for (Terms.Bucket brandBucket : brandBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //??????id
            long brandId = Long.parseLong(brandBucket.getKeyAsString());
            //????????????
            ParsedStringTerms brand_name_agg = brandBucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();

            //????????????
            ParsedStringTerms brand_img_agg = brandBucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();

            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //????????????
        ParsedLongTerms catelogAgg = response.getAggregations().get("catelog_agg");
        List<SearchResult.CatalogVo> catalogVosList = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catelogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            long catelogId = Long.parseLong(bucket.getKeyAsString());
            ParsedStringTerms catelog_name_agg = bucket.getAggregations().get("catelog_name_agg");
            String catelogName = catelog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogId(catelogId);
            catalogVo.setCatalogName(catelogName);
            catalogVosList.add(catalogVo);
        }

        result.setCatalogs(catalogVosList);
        //??????
        result.setPageNum(param.getPageNum());
        // ????????????
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        // ?????????
        int pages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) (total / EsConstant.PRODUCT_PAGESIZE) : (int) (total / EsConstant.PRODUCT_PAGESIZE) + 1;
        //int pages = total % EsConstant.PRODUCT_PAGESIZE == 0 ? total / EsConstant.PRODUCT_PAGESIZE : total / EsConstant.PRODUCT_PAGESIZE + 1
        result.setTotalPages(pages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= pages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        //TODO ?????????????????????
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();

                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                //TODO ????????????product?????? ????????????id??????????????????
                long id = Long.parseLong(s[0]);
                result.getAttrIds().add(id);
                try {
                    R r = productFeignService.info(id);
                    if (r.getCode() == 0) {
                        AttrResponseVo vo = r.getData("attr", new TypeReference<AttrResponseVo>() {
                        });
                        navVo.setNavName(vo.getAttrName());
                    } else {
                        navVo.setNavName(s[0]);
                    }
                    String replace = replaceQueryString(param, attr, "attrs");
                    navVo.setLink("http://search.gulimall.com/list.html?" + replace);
                } catch (Exception e) {
                    log.error("??????????????????");
                }
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            R r = productFeignService.brandsInfos(param.getBrandId());
            List<BrandResponseVo> brandResponseVos = r.getData("brand", new TypeReference<List<BrandResponseVo>>() {
            });
            navVo.setNavName("??????");
            StringBuffer buffer = new StringBuffer();
            String replace = "";
            for (BrandResponseVo brandResponseVo : brandResponseVos) {
                buffer.append(brandResponseVo.getName() + ";");
                replace = replaceQueryString(param, brandResponseVo.getBrandId() + "", "brandId");
            }
            navVo.setNavValue(buffer.toString());
            navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            navs.add(navVo);

        }
        return result;
    }

    @NotNull
    private String replaceQueryString(SearchParam param, String attr, String key)  {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        encode = encode.replace("+", "%20");
        String replace = param.get_queryString().replace("&" + key + "=" + encode, "");
        return replace;
    }


}
