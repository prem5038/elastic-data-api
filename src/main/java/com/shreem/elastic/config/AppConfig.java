package com.shreem.elastic.config;

import com.shreem.elastic.model.RequestQuery;
import com.shreem.elastic.model.ServiceModel;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.*;


@Configuration
public class AppConfig {

    @Value("${elastic.host}")
    String elasticHost;

    @Value("${elastic.port}")
    int elasticPort;

    @Value("${elastic.ssl.schema}")
    String elasticSSLSchema;

    @Value("${elastic.user}")
    String elasticUser;

    @Value("${elastic.password}")
    String elasticPassword;

    @Value("${request.query.file}")
    Resource requestQueryFile;



    @Bean(destroyMethod = "close")
    public RestClient restClient(){
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(elasticUser, elasticPassword));

        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticHost,
                elasticPort, elasticSSLSchema))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        return builder.build();
    }

    @Bean
    public RequestQuery requestQuery() throws IOException {
        LinkedHashMap<String,Object> request = new Yaml().load(requestQueryFile.getInputStream());
        RequestQuery requestQuery = new RequestQuery();
        requestQuery.setApplication((String) request.get("application"));
        List<Map<String,String>> services = (List<Map<String, String>>) request.get("services");
        Map<String, ServiceModel> serviceMap = new HashMap<>();
        for(Map<String,String> entryMap: services){
            ServiceModel service = new ServiceModel();
            service.setId(entryMap.get("id"));
            service.setName(entryMap.get("name"));
            service.setKibanaQuery(entryMap.get("kibanaQuery"));
            service.setSelectFields(entryMap.get("selectFields"));
            serviceMap.put(service.getId(), service);
        }
        requestQuery.setServices(serviceMap);
        return requestQuery;
    }


}
