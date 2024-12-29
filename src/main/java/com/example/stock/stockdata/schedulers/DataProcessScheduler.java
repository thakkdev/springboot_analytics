package com.example.stock.stockdata.schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stock.stockdata.entity.CalculatedDataEntity;
import com.example.stock.stockdata.entity.IBDataEntity;
import com.example.stock.stockdata.repository.CalculatedDataEntityRepository;
import com.example.stock.stockdata.service.AnalyticsService;
import com.example.stock.stockdata.service.TWSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@Component
public class DataProcessScheduler {
    
@Autowired 
private CalculatedDataEntityRepository calculatedDataEntityRepository;
private String symbol;
private int tickerId;
private boolean enabled = false;

private AnalyticsService analyticsServiceWrapper;
private final ObjectMapper objectMapper = new ObjectMapper();
   
public void setAnalyticsServiceWrapper(AnalyticsService analyticsServiceWrapper) 
{ 
    this.analyticsServiceWrapper = analyticsServiceWrapper; 
}

public void setSymbol(String symbol) 
{ 
    this.symbol = symbol; 
}

public void setTickerId(int tickerId) 
{ 
    this.tickerId = tickerId; 
}

public void setEnabled(boolean enabled) 
{ 
    this.enabled = enabled; 
}

@Scheduled(cron = "*/30 * * * * ?") // run every 30 seconds
public void fetchLast1MinuteData() 
{ 
    if(!enabled)
    {
        return;
    }
    //LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1); 
    List<CalculatedDataEntity> data = calculatedDataEntityRepository.findByName(symbol); 
    List<ObjectNode> objectNode = createJsonObjects(data, symbol);
    pushSocketMessage(objectNode);
    log.info("Last 30 seconds data: " + data); 
} 


private List<ObjectNode> createJsonObjects(List<CalculatedDataEntity> entities, String symbol2) 
{ 
    ObjectMapper mapper = new ObjectMapper(); 
    Map<String, Map<Integer, CalculatedDataEntity>> groupedEntities = entities.stream()
    .collect(Collectors.groupingBy(CalculatedDataEntity::getName, 
    Collectors.toMap(CalculatedDataEntity::getMinCount, e -> e))); 
    
    List<ObjectNode> jsonObjects = new ArrayList<>(); 
    
    for (CalculatedDataEntity entity : entities) 
    { 
        ObjectNode jsonObject = mapper.createObjectNode(); 
        jsonObject.put("id", entity.getId()); 
        jsonObject.put("name", entity.getName()); 
        jsonObject.put("minCount", entity.getMinCount()); 
        jsonObject.put("dateTime", entity.getDateTime().toString()); 
        jsonObject.put("volume", entity.getVolume()); 
        jsonObject.put("price", entity.getPrice()); 
        
        if(entity.getMinCount() == 1)
        {
            Double volume1 = getVolumeForMinCount(groupedEntities, entity.getName(), 1); 
            Double volume5 = getVolumeForMinCount(groupedEntities, entity.getName(), 5); 
            Double volume10 = getVolumeForMinCount(groupedEntities, entity.getName(), 10); 

            Double price1 = getPriceForMinCount(groupedEntities, entity.getName(), 1); 
            Double price5 = getPriceForMinCount(groupedEntities, entity.getName(), 5); 
            Double price10 = getPriceForMinCount(groupedEntities, entity.getName(), 10); 
            double volumeDiff1to5 = 0;
            double volumeDiff1to10 = 0;
            double priceDiff1to5 = 0;
            double priceDiff1to10 = 0;

            if (volume1 != null && volume5 != null) 
            { 
                volumeDiff1to5 = ((volume1 - volume5) / volume1) * 100; 
                jsonObject.put("volumeDiff1to5", volumeDiff1to5); 
            } 
        
            if (volume1 != null && volume10 != null) 
            { 
                volumeDiff1to10 = ((volume1 - volume10) / volume1) * 100; 
                jsonObject.put("volumeDiff1to10", volumeDiff1to10); 
            } 

            if (price1 != null && price5 != null) 
            { 
                priceDiff1to5 = ((price1 - price5) / price1) * 100; 
                jsonObject.put("priceDiff1to5", priceDiff1to5); 
            } 
        
            if (price1 != null && price10 != null) 
            { 
                priceDiff1to10 = ((price1 - price10) / price1) * 100; 
                jsonObject.put("priceDiff1to10", priceDiff1to10); 
            } 

            if(volumeDiff1to5 > 0 && volumeDiff1to10 > 0 && priceDiff1to5 > 0
            && priceDiff1to10 > 0)
            {
                jsonObject.put("msg", "Price going up, " + entity.getNote());
            }
            else 
            {
                jsonObject.put("msg", "Price stalling, " + entity.getNote());
            }

        }
        else
        {
            jsonObject.put("msg", "RSI at " + entity.getRsi() + ", " + entity.getNote());
        }
        jsonObjects.add(jsonObject); 
    } 
    return jsonObjects; 
} 


private void pushSocketMessage(List<ObjectNode> objectNode)
{


            String jsonMessage;
            try {
                jsonMessage = objectMapper.writeValueAsString(objectNode);


                System.out.println("Monitor Message is : " + jsonMessage);
            

                analyticsServiceWrapper.sendMessage("monitordatapage", jsonMessage);
            } catch (JsonProcessingException e) {
                log.info(e.getMessage());
            }


}

private Double getVolumeForMinCount(Map<String, Map<Integer, CalculatedDataEntity>> 
groupedEntities, String name, int minCount) 
{ 
    Map<Integer, CalculatedDataEntity> minCountEntities = groupedEntities.get(name); 
    if (minCountEntities != null && minCountEntities.containsKey(minCount)) 
    { 
        return minCountEntities.get(minCount).getVolume(); 
    } 
    return null; 
}

private Double getPriceForMinCount(Map<String, Map<Integer, CalculatedDataEntity>> 
groupedEntities, String name, int minCount) 
{ 
    Map<Integer, CalculatedDataEntity> minCountEntities = groupedEntities.get(name); 
    if (minCountEntities != null && minCountEntities.containsKey(minCount)) 
    { 
        return minCountEntities.get(minCount).getPrice(); 
    } 
    return null; 
}
    

}
