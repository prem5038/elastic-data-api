package com.shreem.elastic.service;

import com.shreem.elastic.model.RequestQuery;
import com.shreem.elastic.model.ServiceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class ElasticDataService {

    @Autowired
    RequestQuery requestQuery;

    @Autowired
    ElasticQueryService elasticQueryService;

    @Value("${response.backup.location}")
    String responseBackupPath;

    public void backupResponse(String services, String ids) throws NoSuchFieldException {
        Predicate<ServiceModel> predicate = this.getPredicate(services, ids);
        System.out.println("services: "+services);
        Map<String,ServiceModel> serviceMap = requestQuery.getServices();
        System.out.println("requestServiceMap size: "+serviceMap.size());
        serviceMap.entrySet()
                .stream()
                .map(es -> es.getValue())
                .filter(predicate)
                .forEach(serviceModel -> this.executeRequestQuery(serviceModel));

    }

    private Predicate<ServiceModel> getPredicate(String services, String ids) throws NoSuchFieldException {
        Predicate<ServiceModel> predicate = null;
        if(!services.isEmpty() && ids.isEmpty()) {
            final List<String> serviceNames = Arrays.asList(services.split(","));
            predicate = serviceModel -> serviceNames.contains(serviceModel.getName())?true:false;
        } else if(services.isEmpty() && !ids.isEmpty()) {
            final List<String> idList = Arrays.asList(ids.split(","));
            predicate = serviceModel -> idList.contains(serviceModel.getId())?true:false;
        } else {
            throw new NoSuchFieldException("Missing query params: services or ids");
        }
        return predicate;
    }

    public void backupResponse() {
        Map<String,ServiceModel> serviceMap = requestQuery.getServices();
        System.out.println("requestServiceMap size: "+serviceMap.size());
        serviceMap.entrySet()
                .stream()
                .map(es -> es.getValue())
                .forEach(serviceModel -> this.executeRequestQuery(serviceModel));
    }

    public void executeRequestQuery(ServiceModel serviceModel){
        System.out.println("Executing query for "+serviceModel.getName());
        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String responseFile = this.responseBackupPath+"/"+serviceModel.getId()+"_"+timestamp+".json";
        String response = this.elasticQueryService.executeRequestQueryForResponse(serviceModel.getKibanaQuery(),
                serviceModel.getSelectFieldSet());
        this.writeToFile(response, responseFile, false);
    }

    public void writeToFile(String response, String fileName, boolean append){
        try {
            File file = new File(fileName);
            boolean isFileReady = false;

            if(file.exists() && append){
                isFileReady = true;
            } else if (!file.exists() && !append) {
                isFileReady = file.createNewFile();
            } else if (file.exists() && !append) {
                if(file.delete())
                    file.createNewFile();
                isFileReady = true;
            } else if(!file.exists() && append){
                throw new NoSuchFileException("File does not exist! New file not created since append enabled");
            }

            if(isFileReady){
                FileWriter myWriter = new FileWriter(file, append);
                myWriter.write(response);
                myWriter.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

}
