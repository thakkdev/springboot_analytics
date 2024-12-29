package com.example.stock.stockdata.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.stock.stockdata.EwrapperImpl;
import com.example.stock.stockdata.entity.IBDataEntity;
import com.example.stock.stockdata.handler.VendorWebSocketHandler;
import com.example.stock.stockdata.repository.IBDataEntityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.ib.client.Contract;
import com.ib.client.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TWSService {
    private final EwrapperImpl ewrapperImpl;
    private VendorWebSocketHandler webSocketHandler;
   
    @Autowired 
    private IBDataEntityRepository ibdataEntityRepository; 

   // @Autowired
    public TWSService(EwrapperImpl ewrapperImpl) {
        this.ewrapperImpl = ewrapperImpl;
        //this.webSocketHandler = webSocketHandler;
        ewrapperImpl.setTwsServiceWrapper(this);
    }

    public void setWebSocketHandler(VendorWebSocketHandler webSocketHandler)
    {
        this.webSocketHandler = webSocketHandler;
    }

    public String connectToTWS(String host, int port, int clientId) {
        return ewrapperImpl.connect(host, port, clientId);
    }
    

    public String disconnectToTWS(String doDisconnect)
    {
        return ewrapperImpl.disconnect();
    }

    public void requestMarketData(int tickerId, Contract contract) {
        
      ewrapperImpl.reqMktData(tickerId, contract);  
      log.info("Test");
    }

    public void cancelMarketData(int tickerId) {
        
        ewrapperImpl.cancelMarketData(tickerId);  
    }

    public void cancelHistMarketData(int tickerId)
    {
        ewrapperImpl.cancelHistMarketData(tickerId);
    }

    public void requestHistMarketData(int tickerId, Contract contract, String backFillEndTime, String backFillDuration,
    String barSizeSetting, String whatToShow, String useRTH, String formatDate, String keepUpToDate) {
        
        int iUseRTH = 0;
        int iFormatDate = 0;
        boolean bKeepUptoDate = false;

        if("1".equals(useRTH))
        {
            iUseRTH = 1;
        }

        if("1".equals(formatDate))
        {
            iFormatDate = 1;
        }

        if("true".equalsIgnoreCase(keepUpToDate))
        {
            bKeepUptoDate = true;
        }

        ewrapperImpl.reqHistMarketData(tickerId, contract, backFillEndTime, backFillDuration, 
        barSizeSetting,whatToShow, iUseRTH, iFormatDate, bKeepUptoDate);        
    }
  

    public boolean isConnectedToTWS()
    {
        return ewrapperImpl.isConnected();
    }

    public void sendMessage(String clientId, String message) 
    { 
        try 
        { 
            webSocketHandler.sendMessage(clientId, message); 
        } 
        catch (IOException e) 
        { 
            log.info(e.getMessage()); 
        } 
    }

    public void placeBuySellOrder( int orderId, Contract contract, Order order)
    {
        if("TRAIL LIMIT".equals(order.orderType().toString()))
        {
                double latestBidPrice = getLatestBidPrice(contract.symbol());
                order.lmtPriceOffset(0.5);
                order.trailStopPrice(latestBidPrice - 0.5);
        }
        else
        {
            if("BUY".equals(order.action().toString()))
            {
                order.lmtPrice(getLatestAskPrice(contract.symbol()));
            }
            else
            {
                order.lmtPrice(getLatestBidPrice(contract.symbol()));
            }
        }

        ewrapperImpl.placeBuySellOrder(orderId, contract, order);
        ewrapperImpl.getAllOpenOrders();

    }

    public void cancelAnOrder(int orderId)
    {
        ewrapperImpl.cancelAnOrder(orderId);
        ewrapperImpl.getAllOpenOrders();
    }

    public void cancelAllOrders()
    {
        ewrapperImpl.cancelAllOrders();
    }

    public double getLatestBidPrice(String symbol)
    {

        //retrieve list of raw data from last 1 min
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1); 
        List<IBDataEntity> data = ibdataEntityRepository.findByTimestampAfter(oneMinuteAgo, symbol);

        //loop through and find latest bid price
        for (IBDataEntity record : data) 
        { 
            JsonNode jsonData = record.getJsonData(); 
            if (jsonData.has("delayedBid") ) 
            { 

                if(jsonData.get("delayedBid") != null)
                {
                    return jsonData.get("delayedBid").asDouble();
                }
            } 
        } 

        return 0.0;
    }

    public double getLatestAskPrice(String symbol)
    {
        //retrieve list of raw data from last 1 min
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1); 
        List<IBDataEntity> data = ibdataEntityRepository.findByTimestampAfter(oneMinuteAgo, symbol);

        //loop through and fimd latest ask price
        for (IBDataEntity record : data) 
        { 
            JsonNode jsonData = record.getJsonData(); 
            if (jsonData.has("delayedAsk")) 
            { 

                if(jsonData.get("delayedAsk") != null)
                {
                    return jsonData.get("delayedAsk").asDouble();
                }
                
            } 
        } 

        return 0.0;
    }
}
