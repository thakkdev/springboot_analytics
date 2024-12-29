package com.example.stock.stockdata.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.stock.stockdata.EwrapperImpl;
import com.example.stock.stockdata.handler.VendorWebSocketHandler;
import com.example.stock.stockdata.schedulers.DataFetchScheduler;
import com.example.stock.stockdata.schedulers.DataProcessScheduler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AnalyticsService {

    private VendorWebSocketHandler webSocketHandler;

    @Autowired
    private DataFetchScheduler dataFetchScheduler;

    @Autowired
    private DataProcessScheduler dataProcessScheduler;

    public AnalyticsService(DataFetchScheduler dataFetchScheduler,DataProcessScheduler dataProcessScheduler ) {
        this.dataFetchScheduler = dataFetchScheduler;
        this.dataProcessScheduler = dataProcessScheduler;

        dataProcessScheduler.setAnalyticsServiceWrapper(this);
    }

    public void setWebSocketHandler(VendorWebSocketHandler webSocketHandler)
    {
        this.webSocketHandler = webSocketHandler;
    }

    public void enableSchedulerData(int tickerId, String symbol)
    {
        dataProcessScheduler.setSymbol(symbol);
        dataFetchScheduler.setSymbol(symbol);
        dataProcessScheduler.setTickerId(tickerId);
        dataProcessScheduler.setEnabled(true);


    }

    public void stopSchedulerData(int tickerId, String symbol)
    {

        dataFetchScheduler.setSymbol(null);
        dataProcessScheduler.setTickerId(0);
        dataProcessScheduler.setEnabled(false);
        
    }

    public void sendMessage(String clientId, String message) 
    { try 
        { webSocketHandler.sendMessage(clientId, message); 
        } 
        catch (IOException e) 
        { 
            log.info(e.getMessage()); 
        } 
    }
   
    
}
