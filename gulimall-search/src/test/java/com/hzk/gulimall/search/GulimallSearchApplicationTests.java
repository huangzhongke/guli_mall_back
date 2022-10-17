package com.hzk.gulimall.search;

import com.alibaba.fastjson.JSONObject;
import com.hzk.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
        System.out.println(client);
    }

    @Test
    public void searchIndexData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //按照年龄值分布聚合

        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));

        TermsAggregationBuilder age = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(age);

        AvgAggregationBuilder balance = AggregationBuilders.avg("balanceAgg").field("balance");
        searchSourceBuilder.aggregation(balance);

        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("搜索条件：" + searchRequest);
        System.out.println(response);
        SearchHits hits = response.getHits();
        for (SearchHit hit : hits) {
            String str = hit.getSourceAsString();
            Account account = JSONObject.parseObject(str, Account.class);
            System.out.println(JSONObject.toJSONString(account));
        }
    }

    @Test
    public void indexData() throws IOException {
        IndexRequest request = new IndexRequest("users");
        User user = new User();
        user.setAge(18);
        user.setGender("男");
        user.setName("jack");
        request.id("1");
        String jsonString = JSONObject.toJSONString(user);
        request.source(jsonString, XContentType.JSON);
        IndexResponse index = client.index(request, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User {
        private String name;
        private Integer age;
        private String gender;
    }
    @Data
    class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }
}