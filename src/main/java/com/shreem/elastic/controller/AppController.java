package com.shreem.elastic.controller;

import com.shreem.elastic.service.ElasticDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class AppController {

    @Autowired
    ElasticDataService elasticDataService;

    @GetMapping(value = "/elastic/response/backup")
    public String createResponseDump(
            @RequestParam(value = "services", defaultValue = "") String services,
            @RequestParam(value = "ids", defaultValue = "") String ids) throws NoSuchFieldException {
        elasticDataService.backupResponse(services, ids);
        return "Request sent!";
    }

    @GetMapping(value = "/elastic/response/backupAll")
    public String createAllResponseDump() {
        elasticDataService.backupResponse();
        return "Request sent!";
    }

    @GetMapping(value = "/elastic/fetch/mappings")
    public String fetchMappingsFromResponseDump(
            @RequestParam(value = "ids", defaultValue = "") String ids){
        elasticDataService.fetchMappings(ids);
        return "Request sent";
    }

}
