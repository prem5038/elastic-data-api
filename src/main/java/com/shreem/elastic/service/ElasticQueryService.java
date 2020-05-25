package com.shreem.elastic.service;

import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class ElasticQueryService {

    @Autowired
    RestClient restClient;

    @Value("${elastic.search.index}")
    String elasticSearchIndex;


    public int getQueryResultsCount(String query) throws IOException {
        Response response = restClient.performRequest(this.createElasticGetCountRequest(query));
        String responseString = asString(response.getEntity().getContent());
        return this.parseQueryResponseForCount(responseString);
    }

    public Set<Map<String,String>> getQueryResults(String query, Set<String> selectFields) throws IOException {
        Response response = restClient.performRequest(this.createElasticGetRequest(query));
        String responseString = asString(response.getEntity().getContent());
        return this.getResultSet(responseString, selectFields);
    }


    public Set<Map<String,String>> getQueryResults(String query) throws IOException {
        return this.getQueryResults(query, null);
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

    private Set<Map<String,String>> getResultSet(String response, Set<String> selectFields){
        Set<Map<String,String>> resultSet = new HashSet<>();
        try{
            JSONArray results = new JSONObject(response).getJSONObject("hits").getJSONArray("hits");
            JSONObject sourcJsonObject = null;
            for(int i=0;i < results.length();i++){
                sourcJsonObject = results.getJSONObject(i).getJSONObject("_source");
                resultSet.add(this.getRecordMap(sourcJsonObject, selectFields));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return resultSet;
    }

    private Map<String,String> getRecordMap(JSONObject jsonObject){
        Set<String> keySet = jsonObject.keySet();
        Map<String,String> recordMap = new HashMap<>();
        for(String key: keySet){
            recordMap.put(key, this.getValue(jsonObject, key));
        }
        return recordMap;
    }

    private Map<String,String> getRecordMap(JSONObject jsonObject, Set<String> selectFields){
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

    public static String asString(InputStream inputStream){
        try(Reader reader = new InputStreamReader(inputStream, UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException ioException){
            throw new UncheckedIOException(ioException);
        }
    }

}
