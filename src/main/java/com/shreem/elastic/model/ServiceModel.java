package com.shreem.elastic.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServiceModel {

    private String id;

    private String name;

    private String kibanaQuery;

    private String selectFields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKibanaQuery() {
        return kibanaQuery;
    }

    public void setKibanaQuery(String kibanaQuery) {
        this.kibanaQuery = kibanaQuery;
    }

    public String getSelectFields() {
        return selectFields;
    }

    public void setSelectFields(String selectFields) {
        this.selectFields = selectFields;
    }

    public Set<String> getSelectFieldSet() {
        String[]  fields = selectFields.split(",");
        return new HashSet<String>(Arrays.asList(fields));
    }

}
