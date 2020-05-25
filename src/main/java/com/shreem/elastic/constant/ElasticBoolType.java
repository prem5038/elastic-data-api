package com.shreem.elastic.constant;

public enum ElasticBoolType {

    FILTER("filter"),
    SHOULD("should");


    private String value;

    ElasticBoolType(String value){
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
