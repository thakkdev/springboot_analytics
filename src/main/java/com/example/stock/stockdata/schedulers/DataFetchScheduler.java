package com.example.stock.stockdata.schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.stock.stockdata.entity.CalculatedDataEntity;
import com.example.stock.stockdata.entity.IBDataEntity;
import com.example.stock.stockdata.repository.CalculatedDataEntityRepository;
import com.example.stock.stockdata.repository.IBDataEntityRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DataFetchScheduler {

    private String symbol = "QQQ"; //default to QQQ

    @Autowired 
    private IBDataEntityRepository ibdataEntityRepository; 

    @Autowired 
    private CalculatedDataEntityRepository calculatedDataEntityRepository;

    public void setSymbol(String symbol) 
    { 
        this.symbol = symbol; 
    }
    
    @Scheduled(cron = "0 * * * * ?") // Every 1 minute 
    public void fetchLast1MinuteData() 
    { 
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1); 
        List<IBDataEntity> data = ibdataEntityRepository.findByTimestampAfter(oneMinuteAgo, symbol); 
        calculateAndSaveAverages(data, symbol, 1);
        log.info("Last 1 minute data: " + data); 
    } 
    
    @Scheduled(cron = "0 */5 * * * ?") // Every 5 minutes 
    public void fetchLast5MinuteData() 
    { 
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5); 
        List<IBDataEntity> data = ibdataEntityRepository.
        findByTimestampAfter(fiveMinutesAgo, symbol); 
        calculateAndSaveAverages(data, symbol, 5);
        log.info("Last 5 minutes data: " + data); 
    } 
    
    @Scheduled(cron = "0 */10 * * * ?") // Every 10 minutes 
    public void fetchLast10MinuteData() 
    { 
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10); 
        List<IBDataEntity> data = ibdataEntityRepository.
        findByTimestampAfter(tenMinutesAgo, symbol); 
        calculateAndSaveAverages(data, symbol, 10);
        log.info("Last 10 minutes data: " + data); 
    }
    
    private double calculateRSI( List<IBDataEntity> data)
    {
        
        List<Double> delayedAskValues = data.stream() 
        .map(IBDataEntity::getJsonData) 
        .filter(node -> node.has("delayedAsk")) 
        .map(node -> node.get("delayedAsk").asDouble()) 
        .collect(Collectors.toList());
        
        double gain = 0; 
        double loss = 0; 
        int dsize = delayedAskValues.size();
        
        for (int i = 1; i < dsize; i++) 
        { 
            double difference = delayedAskValues.get(i) - delayedAskValues.get(i - 1); 
            
            if (difference > 0) 
            { 
                gain = gain + difference; 
            } 
            else 
            { 
                loss = loss - difference; 
            } 
        } 
        gain = gain/ dsize; 
        loss = loss/ dsize; 
        
        double rs = gain / loss; 
        
        return 100 - (100 / (1 + rs));
    }

    private void calculateAndSaveAverages(List<IBDataEntity> data, String symbol, int minutes) 
    { 
        //calculate average price of n minutes
        OptionalDouble averageAsk = data.stream() 
        .map(IBDataEntity::getJsonData) 
        .filter(node -> node.has("delayedAsk")) 
        .mapToDouble(node -> node.get("delayedAsk").asDouble()) 
        .average(); 
        
        //calculate average volume of n minutes
        OptionalDouble averageVolume = data.stream() 
        .map(IBDataEntity::getJsonData) 
        .filter(node -> node.has("delayedVolume")) 
        .mapToDouble(node -> node.get("delayedVolume").asDouble()) 
        .average(); 

        //calculate RSI for 5 and 10 minutes
        double rsi = 0;
        if(minutes == 5 || minutes == 10)
        {
            rsi = calculateRSI(data);
        }

        LocalDateTime now = LocalDateTime.now(); 
        Double avgAsk = averageAsk.orElse(0.0); 
        Double avgVolume = averageVolume.orElse(0.0); 

        CalculatedDataEntity existingRecord = calculatedDataEntityRepository.
        findByNameAndMinCount(symbol, minutes); 
        
        if (existingRecord == null) 
        
        { // Insert new record 
            CalculatedDataEntity newRecord = new CalculatedDataEntity(); 
            newRecord.setName(symbol); 
            newRecord.setMinCount(minutes); 
            newRecord.setDateTime(now); 
            newRecord.setVolume(avgVolume); 
            newRecord.setPrice(avgAsk); 
            newRecord.setRsi(rsi);
            
            calculatedDataEntityRepository.save(newRecord); 
            
            log.info("Inserted %d minute averages - DelayedAsk: %.2f, DelayedVolume: %.2f%n", minutes, avgAsk, avgVolume); 
        
        } 
        else 
        { // Update existing record 
            existingRecord.setDateTime(now); 
            if(existingRecord.getVolume() != null)
            {
                double volDiff = existingRecord.getVolume() - avgVolume;
                if(volDiff > 0)
                {
                    existingRecord.setNote("Volume is going down by :" + volDiff);
                }
                else
                {
                    existingRecord.setNote("Volume is going up by :" + volDiff);
                }
            }
            existingRecord.setVolume(avgVolume); 
            existingRecord.setPrice(avgAsk); 
            existingRecord.setRsi(rsi);
            

            calculatedDataEntityRepository.save(existingRecord); 
            
            log.info("Updated %d minute averages - DelayedAsk: %.2f, DelayedVolume: %.2f%n", minutes, avgAsk, avgVolume);
         } 
    }
}
