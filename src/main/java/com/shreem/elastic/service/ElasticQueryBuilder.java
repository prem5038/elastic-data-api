package com.shreem.elastic.service;

import com.shreem.elastic.constant.ElasticBoolType;
import com.shreem.elastic.constant.ElasticQueryType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ElasticQueryBuilder {


    public JSONObject buildQuery(JSONObject queryJsonObject){
        JSONObject queryHolder = new JSONObject();
        queryHolder.put("query",queryJsonObject);
        return queryHolder;
    }

    public JSONObject buildBoolCriteria(ElasticBoolType elasticBoolType, List<JSONObject> entries){

        JSONObject boolHolder = new JSONObject();
        JSONObject boolJsonObj = new JSONObject();
        JSONArray boolTypeArr = new JSONArray();
        for(JSONObject entry: entries){
            boolTypeArr.put(entry);
        }
        boolJsonObj.put(elasticBoolType.getValue(), boolTypeArr);
        if(elasticBoolType.equals(ElasticBoolType.SHOULD))
            boolJsonObj.put("minimum_should_match", 1);
        boolHolder.put("bool", boolJsonObj);

        return boolHolder;
    }

    public JSONObject buildQueryCriteria(ElasticQueryType elasticQueryType, String field, String value ) {
        JSONObject queryCriteriaHolder = new JSONObject();
        JSONObject queryCriteria = new JSONObject();
        if(elasticQueryType.equals(ElasticQueryType.MATCH_PHRASE)
                || elasticQueryType.equals(ElasticQueryType.MATCH)){
            queryCriteria.put(field, value);
        } else if (elasticQueryType.equals(ElasticQueryType.QUERY_STRING)){
            this.buildQueryStringCriteria(Arrays.asList(new String[]{field}), value);
        }
        queryCriteriaHolder.put(elasticQueryType.getValue(), queryCriteria);
        return queryCriteriaHolder;
    }

    public JSONObject buildQueryStringCriteria(List<String> fields, String value){
        JSONObject queryCriteriaHolder = new JSONObject();
        JSONObject queryCriteria = new JSONObject();
        JSONArray fieldsArr = new JSONArray();
        for(String field: fields) {
            fieldsArr.put(field);
        }
        queryCriteria.put("fields", fieldsArr);
        queryCriteria.put("query",value);
        queryCriteriaHolder.put(ElasticQueryType.QUERY_STRING.getValue(), queryCriteria);
        return queryCriteriaHolder;
    }

    public JSONObject buildRangeCriteria(String fromTimestamp, String toTimestamp){
        JSONObject rangeHolder = new JSONObject();
        JSONObject rangeCriteria = new JSONObject();
        JSONObject timeStampCriteria = new JSONObject();

        timeStampCriteria.put("gte", fromTimestamp);
        timeStampCriteria.put("lte", toTimestamp);
        timeStampCriteria.put("format", "strict_date_optional_time");

        rangeCriteria.put("@timestamp", timeStampCriteria);
        rangeHolder.put("range", rangeCriteria);
        return rangeHolder;
    }

    public List<JSONObject> buildQueryCriterions(String kibanaQuery) throws Exception {
        List<String> criterions = Arrays.asList(kibanaQuery.split(" AND "));
        criterions.forEach(c -> System.out.println(c));
        List<JSONObject> queryCriterions = new LinkedList<>();
        for (String criteria : criterions) {
            String[] criteriaArr = criteria.split(":",2);
            String field = criteriaArr[0].trim().replace("\"","");
            String value = criteriaArr[1].trim().replace("\"","");
            if(!field.equals("timestamp")) {
                if (value.contains("*")) {
                    queryCriterions.add(this.buildQueryStringCriteria(Arrays.asList(field), value));
                } else {
                    queryCriterions.add(this.buildQueryCriteria(ElasticQueryType.MATCH_PHRASE, field, value));
                }
            } else {
                String[] range = value.split(" TO ");
                String from = range[0].replace("[","").replace("]","");
                String to = range[1].replace("[","").replace("]","");
                queryCriterions.add(this.buildRangeCriteria(from,to));
            }
        }
        return queryCriterions;
    }

    public JSONObject buildQuery(String kibanaQuery) throws Exception {
        List<JSONObject> queryCriterions = this.buildQueryCriterions(kibanaQuery);

        List<JSONObject> boolWrappedQueryCriterions = queryCriterions
                .stream()
                .map(criteria -> {
                    if(!criteria.keySet().contains("range"))
                        return this.buildBoolCriteria(ElasticBoolType.SHOULD, Arrays.asList(criteria));
                    else
                        return criteria;
                })
                .collect(Collectors.toList());

        JSONObject boolCriteriaParent = this.buildBoolCriteria(ElasticBoolType.FILTER, boolWrappedQueryCriterions);
        return this.buildQuery(boolCriteriaParent);
    }

}
