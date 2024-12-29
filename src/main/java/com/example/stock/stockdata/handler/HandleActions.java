package com.example.stock.stockdata.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.stock.stockdata.databean.WebSocketMessage;
import com.example.stock.stockdata.service.AnalyticsService;
import com.example.stock.stockdata.service.TWSService;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;

public class HandleActions {


    public void messageTWSServiceActions(WebSocketMessage webSocketMessage, TWSService twsService, AnalyticsService analyticsService)
    {

        if ("requestMarketData".equals(webSocketMessage.getAction())) 
        { int tickerId = webSocketMessage.getTickerId(); 
            String symbol = webSocketMessage.getSymbol(); 
            Contract contract = new Contract();
            contract.symbol(symbol);
            contract.secType("STK");
            contract.currency("USD");
            contract.exchange("SMART");

            twsService.requestMarketData(tickerId, contract);
        }

       else if ("reqHistoricalData".equals(webSocketMessage.getAction())) 
        { int tickerId = webSocketMessage.getTickerId(); 
            String symbol = webSocketMessage.getSymbol(); 
            String backFillDuration = webSocketMessage.getBackFillDuration(); 
            String barSizeSetting = webSocketMessage.getBarSizeSetting(); 
            String useRTH = webSocketMessage.getUseRTH(); 
            String keepUpToDate = webSocketMessage.getKeepUpToDate();
            String whatToShow = "TRADES";
            String formatDate = "1";

            // Get the current timestamp 
            LocalDateTime now = LocalDateTime.now(); 
            // Define a formatter DateTimeFormatter 
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd hh:mm:ss"); 
            // Convert the timestamp to a string 
            String backFillEndTime = now.format(formatter);
            Contract contract = new Contract();
            contract.symbol(symbol);
            contract.secType("STK");
            contract.currency("USD");
            contract.exchange("SMART");
            contract.primaryExch("ARCA");
            
            twsService.requestHistMarketData(tickerId, contract, backFillEndTime, backFillDuration, 
            barSizeSetting, whatToShow, useRTH, formatDate, keepUpToDate);
        }


       else if ("enableSchedulerData".equals(webSocketMessage.getAction())) 
        {   int tickerId = webSocketMessage.getTickerId(); 
            String symbol = webSocketMessage.getSymbol(); 

            analyticsService.enableSchedulerData(tickerId, symbol);

        }

      else if ("stopSchedulerData".equals(webSocketMessage.getAction())) 
        {   int tickerId = webSocketMessage.getTickerId(); 
            String symbol = webSocketMessage.getSymbol(); 

            analyticsService.stopSchedulerData(tickerId, symbol);
            
        }

      else if ("placeBuyOrderData".equals(webSocketMessage.getAction())) 
        {   int orderId = webSocketMessage.getTickerId(); 
            String symbol = webSocketMessage.getSymbol(); 

            Contract contract = new Contract();
            contract.symbol(symbol);
            contract.secType("STK");
            contract.currency("USD");
            contract.exchange("SMART");
            contract.primaryExch("ARCA");
            
            Order order = new Order();
            order.action("BUY");
            order.orderType("LMT"); // Limit order
            //order.lmtPrice(parseStringToMaxDouble(webSocketMessage.getLimitPrice())); // Limit price
            order.totalQuantity(Decimal.parse(webSocketMessage.getOrderSize().trim()));
            order.usePriceMgmtAlgo(true);
            
            twsService.placeBuySellOrder(orderId, contract, order);
        }

      else if ("placeSellOrderData".equals(webSocketMessage.getAction())) 
        {   int orderId = webSocketMessage.getTickerId(); 
            String symbol = webSocketMessage.getSymbol(); 

            Contract contract = new Contract();
            contract.symbol(symbol);
            contract.secType("STK");
            contract.currency("USD");
            contract.exchange("SMART");
            contract.primaryExch("ARCA");
            
            Order order = new Order();
            order.action("SELL");
            order.orderType("LMT"); // Limit order
            //order.lmtPrice(parseStringToMaxDouble(webSocketMessage.getLimitPrice())); // Limit price
            order.totalQuantity(Decimal.parse(webSocketMessage.getOrderSize().trim()));
            order.usePriceMgmtAlgo(true);

            twsService.placeBuySellOrder(orderId, contract, order);
                       
        }
        else if ("cancelAnOrderData".equals(webSocketMessage.getAction())) 
        {   int orderId = webSocketMessage.getTickerId(); 
     
            //request global cancel
            twsService.cancelAnOrder(orderId);
                       
        }

        else if ("cancelAllOrderData".equals(webSocketMessage.getAction())) 
        {

            //request global cancel
            twsService.cancelAllOrders();              
        }
        else if ("setTrailLimit".equals(webSocketMessage.getAction()))
        {
              int orderId = webSocketMessage.getTickerId(); 
                String symbol = webSocketMessage.getSymbol(); 
    
                Contract contract = new Contract();
                contract.symbol(symbol);
                contract.secType("STK");
                contract.currency("USD");
                contract.exchange("SMART");
                contract.primaryExch("ARCA");
                
                Order order = new Order();
                order.action("SELL");
                order.orderType("TRAIL LIMIT"); // Trailing Limit order
                //order.lmtPrice(parseStringToMaxDouble(webSocketMessage.getLimitPrice())); // Limit price
                order.totalQuantity(Decimal.parse(webSocketMessage.getOrderSize().trim()));
                order.usePriceMgmtAlgo(true);
                order.trailingPercent(parseStringToMaxDouble(webSocketMessage.getTrailingPercent())); // Trailing percentage
               
                twsService.placeBuySellOrder(orderId, contract, order);
        

        }

    }    

    private double parseStringToMaxDouble(String value) {
            double maxValue = Double.MAX_VALUE; // Define a maximum value if needed
    
            try {
                double parsedValue = Double.parseDouble(value.trim());
                return Math.min(parsedValue, maxValue); // Ensure the value does not exceed maxValue
            } catch (NumberFormatException e) {
                // Handle the error, e.g., by logging it or rethrowing as a different exception
                System.err.println("Invalid number format: " + value);
                return 0.0; // Return a default value or handle it as needed
            }
        }
    
    
}
