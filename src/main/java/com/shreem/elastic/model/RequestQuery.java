package com.shreem.elastic.model;

import java.util.Map;

public class RequestQuery {

    private String application;

    private Map<String, ServiceModel> services;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Map<String, ServiceModel> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceModel> services) {
        this.services = services;
    }
}
