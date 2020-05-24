package com.shreem.elastic.config;

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



@Configuration
public class ElasticConfig {

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


}
