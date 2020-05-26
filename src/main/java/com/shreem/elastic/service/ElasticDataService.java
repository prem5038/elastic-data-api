package com.shreem.elastic.service;

import com.shreem.elastic.model.RequestQuery;
import com.shreem.elastic.model.ServiceModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
            System.out.println("An error occurred."+fileName);
            e.printStackTrace();
        }

    }


    public void fetchMappings(String ids){
        Set<String> selectFieldSet = new HashSet<>();
        selectFieldSet.add("message");
        selectFieldSet.add("agent");

        Predicate<File> predicate = file -> !file.isDirectory();

        if(!ids.isEmpty()) {
            List<String> idList = Arrays.asList(ids.split(","));
            predicate = file -> {
                boolean isPresent = false;
                for (String id : idList) {
                    isPresent = file.getName().contains(id);
                }
                return isPresent;
            };
        }

            File folder = new File(responseBackupPath);
            List<File> queryResponseFiles = Arrays.asList(folder.listFiles());


            queryResponseFiles.stream().filter(predicate).forEach(file -> {
                try {
                    String contents = new String(Files.readAllBytes(Paths.get(file.getPath())));
                    Set<Map<String,String>> resultSet = this.elasticQueryService.parseQueryResponseForResultSet(contents, selectFieldSet);
                    Set<String> contentSet = resultSet.stream().map(m -> m.get("message")+", "+m.get("agent")).collect(Collectors.toSet());

                    StringBuilder stringBuilder = new StringBuilder();
                    for(String content: contentSet){
                        stringBuilder.append(content);
                    }
                    String mappingsFileName = file.getName().substring(0,file.getName().indexOf(".json"))+"_mappings.txt";
                    this.writeToFile(stringBuilder.toString(),responseBackupPath+"/mappings/"+mappingsFileName,false);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

    }



}
