package com.shreem.elastic.constant;

public enum ElasticQueryType {

    QUERY_STRING("query_string"),
    MATCH_PHRASE("match_phrase"),
    MATCH("match");

    private String value;

    ElasticQueryType(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

}
