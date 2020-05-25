package com.shreem.elastic.service;

import com.shreem.elastic.constant.ElasticBoolType;
import com.shreem.elastic.constant.ElasticQueryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class ElasticServiceChecker {

    @Autowired
    ElasticQueryService elasticQueryService;

    @Autowired
    ElasticQueryBuilder queryBuilder;

    @Value("${request.match_all}")
    Resource requestMatchAll;

    @PostConstruct
    public void init() throws Exception {
        System.out.println("Elastic Checker ....");
        //this.checkElasticQueryService();
        System.out.println("Query: ");
        String searchQuery = "message: \"*Windows*\" AND agent: \"*Gecko\" AND timestamp: [\"2015-11-04T11:12:13\" TO \"2015-11-04T11:20:20\"]";

        System.out.println(queryBuilder.buildQuery(searchQuery));
    }







    private void checkElasticQueryService() throws IOException {
        String requestStr = ElasticQueryService.asString(requestMatchAll.getInputStream());
        System.out.println("Request: ");
        System.out.println(requestStr);
        Set<String> selectFields = new HashSet<>();
        selectFields.add("message");

        System.out.println("COUNT: "+elasticQueryService.getQueryResultsCount(requestStr));

        Set<Map<String,String>> resultSet = elasticQueryService.getQueryResults(requestStr, selectFields);
    }








}
