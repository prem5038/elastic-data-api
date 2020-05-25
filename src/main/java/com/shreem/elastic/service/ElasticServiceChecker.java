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

        System.out.println("Query: ");
        String searchQuery = "message: \"*Windows*\" AND agent: \"*Gecko*\" AND timestamp: [\"2020-05-25T12:47:53.238Z\" TO \"2020-05-25T13:47:53.238Z\"]";

        this.checkElasticQueryService(searchQuery);

    }







    private void checkElasticQueryService(String kibanaQuery) throws Exception {
        //String requestStr = ElasticQueryService.asString(requestMatchAll.getInputStream());
        String requestStr = queryBuilder.buildQuery(kibanaQuery).toString();
        System.out.println("Request: ");
        System.out.println(requestStr);
        Set<String> selectFields = new HashSet<>();
        selectFields.add("message");

        System.out.println("COUNT: "+elasticQueryService.getQueryResultsCount(requestStr));

        Set<Map<String,String>> resultSet = elasticQueryService.getQueryResults(requestStr, selectFields);
    }








}
