package com.shreem.elastic.service;

import com.shreem.elastic.model.RequestQuery;
import com.shreem.elastic.model.ServiceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

@Component
public class ElasticServiceChecker {

    @Autowired
    ElasticQueryService elasticQueryService;

    @Autowired
    RequestQuery requestQuery;


    @PostConstruct
    public void init() throws Exception {
        System.out.println("Elastic Checker ....");


    }





}
