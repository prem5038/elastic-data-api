package com.shreem.elastic.service;

import com.shreem.elastic.util.AppUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ElasticQueryService {

    @Autowired
    RestClient restClient;

    @Autowired
    ElasticQueryBuilder elasticQueryBuilder;

    @Value("${elastic.search.index}")
    String elasticSearchIndex;

    public Set<Map<String,String>> executeRequestQueryForResultSet(String kibanaQuery, Set<String> selectFields) throws Exception {
        String requestStr = elasticQueryBuilder.buildQuery(kibanaQuery).toString();
        System.out.println("Request: ");
        System.out.println(requestStr);
        System.out.println("COUNT: "+this.getQueryResultsCount(requestStr));
        Set<Map<String,String>> resultSet = this.getQueryResults(requestStr, selectFields);
        return resultSet;
    }

    public String executeRequestQueryForResponse(String kibanaQuery, Set<String> selectFields) {
        String query = null;
        try {
            String requestStr = elasticQueryBuilder.buildQuery(kibanaQuery).toString();
            query = this.getQueryResponse(requestStr);
        } catch (Exception e){
            e.printStackTrace();
        }
        return query;
    }

    public int getQueryResultsCount(String query) throws IOException {
        Response response = restClient.performRequest(this.createElasticGetCountRequest(query));
        String responseString = AppUtils.asString(response.getEntity().getContent());
        return this.parseQueryResponseForCount(responseString);
    }

    public String getQueryResponse(String query) throws IOException {
        Response response = restClient.performRequest(this.createElasticGetRequest(query));
        return AppUtils.asString(response.getEntity().getContent());
    }

    public Set<Map<String,String>> getQueryResults(String query, Set<String> selectFields) throws IOException {
        return this.parseQueryResponseForResultSet(this.getQueryResponse(query), selectFields);
    }

    private Request createElasticGetRequest(String requestJson){
        Request request = new Request("GET", "/" + elasticSearchIndex+ "/_search");
        request.addParameter("pretty","true");
        request.setJsonEntity(requestJson);
        return request;
    }

    private Request createElasticGetCountRequest(String requestJson){
        Request request = new Request("GET", "/" + elasticSearchIndex+ "/_count");
        request.addParameter("pretty","true");
        request.setJsonEntity(requestJson);
        return request;
    }

    private int parseQueryResponseForCount(String response){
        return new JSONObject(response).getInt("count");
    }

    private Set<Map<String,String>> parseQueryResponseForResultSet(String response, Set<String> selectFields){
        Set<Map<String,String>> resultSet = new HashSet<>();
        try{
            JSONArray results = new JSONObject(response).getJSONObject("hits").getJSONArray("hits");
            JSONObject sourcJsonObject = null;
            for(int i=0;i < results.length();i++){
                sourcJsonObject = results.getJSONObject(i).getJSONObject("_source");
                resultSet.add(this.parseQueryResultForRecordMap(sourcJsonObject, selectFields));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return resultSet;
    }

    private Map<String,String> parseQueryResultForRecordMap(JSONObject jsonObject, Set<String> selectFields){
        Set<String> keySet = null;

        if(selectFields!=null && !selectFields.isEmpty()) {
            keySet = jsonObject.keySet()
                    .stream()
                    .filter(key -> selectFields.contains(key) ? true : false)
                    .collect(Collectors.toSet());
        } else {
            keySet = jsonObject.keySet();
        }

        Map<String,String> recordMap = new HashMap<>();
        for(String key: keySet){
            recordMap.put(key, this.getValue(jsonObject, key));
        }
        return recordMap;
    }

    private String getValue(JSONObject sourceJsonObject, String attribute){
        String returnValue = "";
        try {
            returnValue = sourceJsonObject.getString(attribute);
        } catch (Exception e){
            e.printStackTrace();
        }
        return returnValue;
    }



}
