package com.example.stock.stockdata;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.stock.stockdata.entity.IBDataEntity;
import com.example.stock.stockdata.handler.VendorWebSocketHandler;
import com.example.stock.stockdata.parser.StreamingDataParser;
import com.example.stock.stockdata.repository.IBDataEntityRepository;
import com.example.stock.stockdata.service.TWSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.example.stock.stockdata.handler.VendorWebSocketHandler;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;



@Slf4j
@Component
@Scope("singleton")
public final class EwrapperImpl implements EWrapper{

    private final EReaderSignal m_signal = new EJavaSignal();
    private final EClientSocket m_client = new EClientSocket(this, m_signal);
    private final AtomicInteger autoIncrement = new AtomicInteger();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Integer, String> symbolMap = new HashMap<Integer, String>();

    @Autowired
    private IBDataEntityRepository jsonDataRepository;

    private static Map<String, Object> jsonMsgObject = new HashMap<String, Object>();



    private static double histDwn = 0;
  

   // private final VendorWebSocketHandler webSocketHandler;

   private TWSService twsServiceWrapper;
   
   public void setTwsServiceWrapper(TWSService twsServiceWrapper) 
   { 
        this.twsServiceWrapper = twsServiceWrapper; 
   }

   public boolean isConnected() 
   { 
        return m_client.isConnected(); 
   }

    //protected int currentOrderId = -1;

    // public EClientSocket getClientSocket() 
    // {
    //      return m_client; 
    // }

    @Value("localhost")
    private String TWS_HOST;

    @Value("4002")
    private int TWS_PORT;

    public String connect(String host, int port, int clientId) 
    { 
        String status = "connected";

        System.out.println("Test");
        try {
            connect();
        } catch (InterruptedException e) {

            status = e.getMessage();;
        }
        return status;
    } 
  
    //@PostConstruct
    private void connect() throws InterruptedException 
    { 
      //  m_client.setConnectOptions("+PACEAPI");
        //m_client.optionalCapabilities("");
        //m_client.eConnect(host, port, clientId); 
        
         m_client.eConnect(TWS_HOST, TWS_PORT, 0); 

         final EReader reader = new EReader(m_client, m_signal);
          reader.start();
    
           
        // An additional thread is created in this program design to empty the messaging
        // queue
        new Thread(() -> {
            while (m_client.isConnected()) {
                m_signal.waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }).start();

        Thread.sleep(2000); 
        
            if (m_client.isConnected()) 
            { System.out.println("Connected to TWS"); 
            
        
        } 
            else 
            { System.out.println("Failed to connect to TWS, retrying..."); 
        } 
       
            

       // return connectinfo; 
        
    }


    public String disconnect()
    {
        m_client.eDisconnect();
        return "Disconnected";
    }

       // @Override
    // public TwsResultHolder<List<Contract>> searchContract(String search) {
    //     if (StringUtils.hasLength(search)) {
    //         final int currentId = autoIncrement.getAndIncrement();
    //         client.reqMatchingSymbols(currentId, search);
    //         return twsResultHandler.getResult(currentId);
    //     }
    //     return new TwsResultHolder("Search parameter cannot be empty");
    // }

    // @Override
    // public TwsResultHolder<ContractHolder> requestContractByConid(int conid) {
    //     Contract contract = new Contract();
    //     contract.conid(conid);
    //     TwsResultHolder<ContractDetails> contractDetails = requestContractDetails(contract);
    //     ContractHolder contractHolder = new ContractHolder(contractDetails.getResult().contract());
    //     contractHolder.setDetails(contractDetails.getResult());
    //     return new TwsResultHolder<>(contractHolder);
    // }

    // @Override
    // public TwsResultHolder<ContractDetails> requestContractDetails(Contract contract) {
    //     final int currentId = autoIncrement.getAndIncrement();
    //     client.reqContractDetails(currentId, contract);
    //     TwsResultHolder<ContractDetails> details = twsResultHandler.getResult(currentId);
    //     Optional<ContractHolder> contractHolder = contractRepository.findById(details.getResult().conid());
    //     contractHolder.ifPresent(holder -> {
    //         holder.setDetails(details.getResult());
    //         // TODO save from ContractManager
    //         contractRepository.save(holder);
    //     });
    //     return details;
    // }


    public int reqMktData(int tickerId, Contract contract) {
       // final int currentId = autoIncrement.getAndIncrement();
       m_client.reqMarketDataType(3);
        symbolMap.put(Integer.valueOf(tickerId), contract.symbol());
        m_client.reqMktData(tickerId, contract, "", false, false, null);
        return tickerId;
    }

    public void reqHistMarketData(int tickerId, Contract contract, String backFillEndTime, String backFillDuration,
    String barSizeSetting, String whatToShow, int useRTH, int formatDate, boolean keepUpToDate)
    {
        //reset prev Dwn value to 0
        histDwn = 0;

        //symbolMap.put(Integer.valueOf(tickerId), contract.symbol());

        m_client.reqHistoricalData(tickerId, contract, backFillEndTime, backFillDuration, 
        barSizeSetting, whatToShow,0, 1, false, null);

    }

    public void cancelMarketData(int tickerId)
    {
        m_client.cancelMktData(tickerId);
    }

    public void cancelHistMarketData(int tickerId)
    {
        m_client.cancelHistoricalData(tickerId);
    }


    public void placeBuySellOrder(int orderId, Contract contract, Order order)
    {
        m_client.placeOrder(orderId, contract, order);

    }

    public void cancelAnOrder(int orderId)
    {
        OrderCancel oc = new OrderCancel();
        
        m_client.cancelOrder(orderId, oc);
        
    }

    public void cancelAllOrders()
    {
        OrderCancel oc = new OrderCancel();
        m_client.reqGlobalCancel(oc);
        
    }

    public void getAllOpenOrders()
    {
        m_client.reqAllOpenOrders();
    }


    // @Override
    // public int subscribeMarketData(Contract contract, boolean tickByTick) {
    //     final int currentId = autoIncrement.getAndIncrement();
    //     Optional<ContractHolder> contractHolderOptional = contractRepository.findById(contract.conid());
    //     ContractHolder contractHolder = contractHolderOptional.orElse(new ContractHolder(contract));
    //     contractHolder.setStreamRequestId(currentId);
    //     contractRepository.save(contractHolder);
    //     try {
    //         timeSeriesHandler.createStream(currentId, contract);
    //     } catch (JedisDataException e) {
    //         log.error(e.getMessage());
    //     }
    //     if (tickByTick) {
    //         client.reqTickByTickData(currentId, contract, "BidAsk", 1, false);
    //     } else {
    //         client.reqMktData(currentId, contract, "", false, false, null);
    //     }
    //     return currentId;
    // }

    // @Override
    // public Collection<Option> requestForOptionChain(Contract underlying) {
    //     final int currentId = autoIncrement.getAndIncrement();
    //     client.reqSecDefOptParams(currentId, underlying.symbol(), "", //underlying.exchange(),
    //             underlying.secType().getApiString(), underlying.conid());

    //     TwsResultHolder resultHolder = twsResultHandler.getResult(currentId);
    //     return (Collection<Option>) resultHolder.getResult();
    // }

    // -- TWS callbacks

    @Override
    public void connectAck() {
        if (m_client.isAsyncEConnect()) {
            log.info("Acknowledging connection");
            m_client.startAPI();
        }
    }

    // ! [tickprice]
    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
        TickType tickType = TickType.get(field);
        if (Set.of(TickType.ASK, TickType.BID).contains(tickType)) {
           // timeSeriesHandler.addToStream(tickerId, price, tickType);
            log.debug("Tick added to stream {}: {}", tickType, price);
        } else {
            log.debug("Skip tick type {}", tickType);
        }

        String msg = EWrapperMsgGenerator.tickPrice( tickerId, field, price, attribs);

        pushMarketDataMessage(tickerId, msg);
        
        log.info( msg);


    }
    // ! [tickprice]

    // ! [ticksize]

    @Override
    public void tickSize(int tickerId, int field, Decimal size) {
        TickType tickType = TickType.get(field);
      //  log.info("Tick Size. Ticker Id:" + tickerId + ", Field: " + tickType + ", Size: " + size);

        String msg = EWrapperMsgGenerator.tickSize( tickerId, field, size);

        pushMarketDataMessage(tickerId, msg);
        
        log.info( msg);

    }


 //Send message to Websocket
 private void pushMarketDataMessage(int tickerId, String msg)
 {
     
     boolean startNewObject = StreamingDataParser.parseMessagesToJson(msg, jsonMsgObject);
     if (startNewObject)
     {
         //create a JSON message and push to websocket
         try {
            String jsonMessage = objectMapper.writeValueAsString(jsonMsgObject);
             //System.out.println("Message is : " + jsonMessage);

             Map<String, Object> sockmessage = new HashMap<>(); 
             sockmessage.put("tickerId", tickerId); 
             sockmessage.put("data", jsonMessage); 
             String jsonSockMessage = objectMapper.writeValueAsString(sockmessage);
             

             twsServiceWrapper.sendMessage("rawdatapage", jsonSockMessage);

             IBDataEntity entity = new IBDataEntity(); 
             // Convert JSON string to JsonNode 
             JsonNode jsonNode = objectMapper.readTree(jsonMessage);
             entity.setJsonData(jsonNode); 
             entity.setDateTime(LocalDateTime.now());
             entity.setName(symbolMap.get(Integer.valueOf(tickerId)));
             jsonDataRepository.save(entity);


         } catch (JsonProcessingException e) {
             log.info(e.getMessage());
         }
     
         //create new JSONObject
         jsonMsgObject = new HashMap<String, Object>();
 
      }


    

 }

    //Send message to Websocket
    private void pushHistoricalDataMessage(int reqId, Bar bar)
    {
        Map<String, Object> histJsonMsgObject = new HashMap<String, Object>();
    
        double cutIn = bar.open() + (histDwn * bar.open());  

        double dwn = (bar.open() - bar.low()) / bar.open() ;

        //assign prevous value for next bar use
        histDwn = dwn;

        histJsonMsgObject.put("reqId", reqId);
        histJsonMsgObject.put("date", bar.time());
        histJsonMsgObject.put("open", bar.open());
        histJsonMsgObject.put("high", bar.high());
        histJsonMsgObject.put("low", bar.low());
        histJsonMsgObject.put("close", bar.close());
        histJsonMsgObject.put("volume", bar.volume().toString());
        histJsonMsgObject.put("count", bar.count());
        histJsonMsgObject.put("wap", bar.wap().toString());
        histJsonMsgObject.put("cutIn", cutIn);

        //create a JSON message and push to websocket
        try {
          String jsonMessage = objectMapper.writeValueAsString(histJsonMsgObject);
          System.out.println("Hist Message is : " + jsonMessage);
              
          twsServiceWrapper.sendMessage("histdatapage", jsonMessage);

            } catch (JsonProcessingException e) {
                log.info(e.getMessage());
            }
    

    }

    private void pushBuySellSockMessage(String msg)
    {
        Map<String, Object> sockmessage = new HashMap<>(); 
        sockmessage.put("msg", msg); 
        String jsonSockMessage;
        try {
            jsonSockMessage = objectMapper.writeValueAsString(sockmessage);
            twsServiceWrapper.sendMessage("buyselldatapage", jsonSockMessage); 
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
        }

    }
    // ! [ticksize]

    // ! [tickoptioncomputation]

    public void tickOptionComputation(int tickerId, int field,
            double impliedVol, double delta, double optPrice,
            double pvDividend, double gamma, double vega, double theta,
            double undPrice) {
        log.info("TickOptionComputation. TickerId: " + tickerId + ", field: " + field + ", ImpliedVolatility: "
                + impliedVol + ", Delta: " + delta
                + ", OptionPrice: " + optPrice + ", pvDividend: " + pvDividend + ", Gamma: " + gamma + ", Vega: " + vega
                + ", Theta: " + theta + ", UnderlyingPrice: " + undPrice);
    }
    // ! [tickoptioncomputation]

    // ! [tickgeneric]
    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
        log.info("Tick Generic. Ticker Id:" + tickerId + ", Field: " + TickType.getField(tickType) + ", Value: "
                + value);
        
        String msg = EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value);
        pushMarketDataMessage(tickerId, msg);
        log.info( msg);
    }
    // ! [tickgeneric]

    // ! [tickstring]
    @Override
    public void tickString(int tickerId, int tickType, String value) {
        TickType type = TickType.get(tickType);
        // log.info("Tick string. Ticker Id:" + tickerId + ", Type: " + type.name() + ",
        // Value: " + value);

        String msg = EWrapperMsgGenerator.tickString(tickerId, tickType, value);
        pushMarketDataMessage(tickerId, msg);
        log.info(msg);
    }

    // ! [tickstring]
    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints,
            String formattedBasisPoints, double impliedFuture, int holdDays,
            String futureLastTradeDate, double dividendImpact,
            double dividendsToLastTradeDate) {
        log.info("TickEFP. " + tickerId + ", Type: " + tickType + ", BasisPoints: " + basisPoints
                + ", FormattedBasisPoints: " +
                formattedBasisPoints + ", ImpliedFuture: " + impliedFuture + ", HoldDays: " + holdDays
                + ", FutureLastTradeDate: " + futureLastTradeDate +
                ", DividendImpact: " + dividendImpact + ", DividendsToLastTradeDate: " + dividendsToLastTradeDate);
    }

    // ! [orderstatus]

    // public void orderStatus1(int orderId, String status, double filled,
    //         double remaining, double avgFillPrice, int permId, int parentId,
    //         double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
    //     //orderManagerService.changeOrderStatus(permId, status, filled, remaining, avgFillPrice, lastFillPrice);
    // }
    // ! [orderstatus]

    // ! [openorder]
    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
       //orderManagerService.setOrder(contract, order, orderState);
       String msg = EWrapperMsgGenerator.openOrder( orderId, contract, order, orderState);
       pushBuySellSockMessage(msg);
       log.info(msg);

    }



    // ! [openorder]

    // ! [openorderend]
    @Override
    public void openOrderEnd() {
        log.info("Order list retrieved");
        String msg = EWrapperMsgGenerator.openOrderEnd();
        pushBuySellSockMessage(msg); 
        log.info(msg);
    }
    // ! [openorderend]

    // ! [updateaccountvalue]
    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        log.info("UpdateAccountValue. Key: " + key + ", Value: " + value + ", Currency: " + currency + ", AccountName: "
                + accountName);
    }
    // ! [updateaccountvalue]

    // ! [updateportfolio]

    public void updatePortfolio(Contract contract, double position,
            double marketPrice, double marketValue, double averageCost,
            double unrealizedPNL, double realizedPNL, String accountName) {
        log.info("UpdatePortfolio. " + contract.symbol() + ", " + contract.secType() + " @ " + contract.exchange()
                + ": Position: " + position + ", MarketPrice: " + marketPrice + ", MarketValue: " + marketValue
                + ", AverageCost: " + averageCost
                + ", UnrealizedPNL: " + unrealizedPNL + ", RealizedPNL: " + realizedPNL + ", AccountName: "
                + accountName);
    }
    // ! [updateportfolio]

    // ! [updateaccounttime]
    @Override
    public void updateAccountTime(String timeStamp) {
        log.info("UpdateAccountTime. Time: " + timeStamp + "\n");
    }
    // ! [updateaccounttime]

    // ! [accountdownloadend]
    @Override
    public void accountDownloadEnd(String accountName) {
        log.info("Account download finished: " + accountName + "\n");
    }
    // ! [accountdownloadend]

    // ! [nextvalidid]
    @Override
    public void nextValidId(int orderId) {
        pushBuySellSockMessage("Next Order Id is: " +  String.valueOf(orderId));
    }
    // ! [nextvalidid]

    // ! [contractdetails]
    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        //twsResultHandler.setResult(reqId, new TwsResultHolder<ContractDetails>(contractDetails));
    }

    // ! [contractdetails]
    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
        log.info(EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails));
    }

    // ! [contractdetailsend]
    @Override
    public void contractDetailsEnd(int reqId) {
        log.info("ContractDetailsEnd. " + reqId + "\n");
    }
    // ! [contractdetailsend]

    // ! [execdetails]
    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        log.info("ExecDetails. " + reqId + " - [" + contract.symbol() + "], [" + contract.secType() + "], ["
                + contract.currency() + "], [" + execution.execId() +
                "], [" + execution.orderId() + "], [" + execution.shares() + "]" + ", [" + execution.lastLiquidity()
                + "]");
        String msg = EWrapperMsgGenerator.execDetails(reqId, contract, execution);
        pushBuySellSockMessage(msg);
    }
    // ! [execdetails]

    // ! [execdetailsend]
    @Override
    public void execDetailsEnd(int reqId) {
        log.info("ExecDetailsEnd. " + reqId + "\n");
    }
    // ! [execdetailsend]

    // ! [updatemktdepth]
    public void updateMktDepth(int tickerId, int position, int operation,
            int side, double price, int size) {
        log.info("UpdateMarketDepth. " + tickerId + " - Position: " + position + ", Operation: " + operation
                + ", Side: " + side + ", Price: " + price + ", Size: " + size + "");
    }
    // ! [updatemktdepth]

    // ! [updatemktdepthl2]
 
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price,
            int size, boolean isSmartDepth) {
        log.info("UpdateMarketDepthL2. " + tickerId + " - Position: " + position + ", Operation: " + operation
                + ", Side: " + side + ", Price: " + price + ", Size: " + size + ", isSmartDepth: " + isSmartDepth);
    }
    // ! [updatemktdepthl2]

    // ! [updatenewsbulletin]
    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        log.info("News Bulletins. " + msgId + " - Type: " + msgType + ", Message: " + message + ", Exchange of Origin: "
                + origExchange + "\n");
    }
    // ! [updatenewsbulletin]

    // ! [managedaccounts]
    @Override
    public void managedAccounts(String accountsList) {
        log.info("Account list: " + accountsList);
    }
    // ! [managedaccounts]

    // ! [receivefa]
    @Override
    public void receiveFA(int faDataType, String xml) {
        log.info("Receiving FA: " + faDataType + " - " + xml);
    }
    // ! [receivefa]

    // ! [historicaldata]
    @Override
    public void historicalData(int reqId, Bar bar) {
        log.info("HistoricalData. " + reqId + " - Date: " + bar.time() + ", Open: " + bar.open() + ", High: "
                + bar.high() + ", Low: " + bar.low() + ", Close: " + bar.close() + ", Volume: " + bar.volume()
                + ", Count: " + bar.count() + ", WAP: " + bar.wap());

        pushHistoricalDataMessage(reqId, bar);

    }
    // ! [historicaldata]

    // ! [historicaldataend]
    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
        log.info("HistoricalDataEnd. " + reqId + " - Start Date: " + startDateStr + ", End Date: " + endDateStr);
    }
    // ! [historicaldataend]

    // ! [scannerparameters]
    @Override
    public void scannerParameters(String xml) {
        log.info("ScannerParameters. " + xml + "\n");
    }
    // ! [scannerparameters]

    // ! [scannerdata]
    @Override
    public void scannerData(int reqId, int rank,
            ContractDetails contractDetails, String distance, String benchmark,
            String projection, String legsStr) {
        log.info("ScannerData. " + reqId + " - Rank: " + rank + ", Symbol: " + contractDetails.contract().symbol()
                + ", SecType: " + contractDetails.contract().secType() + ", Currency: "
                + contractDetails.contract().currency()
                + ", Distance: " + distance + ", Benchmark: " + benchmark + ", Projection: " + projection
                + ", Legs String: " + legsStr);
    }
    // ! [scannerdata]

    // ! [scannerdataend]
    @Override
    public void scannerDataEnd(int reqId) {
        log.info("ScannerDataEnd. " + reqId);
    }
    // ! [scannerdataend]

    // ! [realtimebar]

    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume,
            double wap, int count) {
        log.info("RealTimeBars. " + reqId + " - Time: " + time + ", Open: " + open + ", High: " + high + ", Low: " + low
                + ", Close: " + close + ", Volume: " + volume + ", Count: " + count + ", WAP: " + wap);
    }

    // ! [realtimebar]
    @Override
    public void currentTime(long time) {
        log.info("currentTime");
    }

    // ! [fundamentaldata]
    @Override
    public void fundamentalData(int reqId, String data) {
        log.info("FundamentalData. ReqId: [" + reqId + "] - Data: [" + data + "]");
    }

    // ! [fundamentaldata]
    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {
        log.info("deltaNeutralValidation");
    }

    // ! [ticksnapshotend]
    @Override
    public void tickSnapshotEnd(int reqId) {
        log.info("TickSnapshotEnd: " + reqId);
    }
    // ! [ticksnapshotend]

    // ! [marketdatatype]
    @Override
    public void marketDataType(int reqId, int marketDataType) {
        log.info("MarketDataType. [" + reqId + "], Type: [" + marketDataType + "]\n");
    }
    // ! [marketdatatype]

    // ! [commissionreport]
    @Override
    public void commissionReport(CommissionReport commissionReport) {
        log.info("CommissionReport. [" + commissionReport.execId() + "] - [" + commissionReport.commission() + "] ["
                + commissionReport.currency() + "] RPNL [" + commissionReport.realizedPNL() + "]");
    }
    // ! [commissionreport]

    // ! [position]

    public void position(String account, Contract contract, double pos, double avgCost) {
        //positionManagerService.addPosition(new PositionHolder(contract, pos, avgCost));
    }
    // ! [position]

    // ! [positionend]
    @Override
    public void positionEnd() {
        log.info("Position list retrieved");
    }
    // ! [positionend]

    // ! [accountsummary]
    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        log.info("Acct Summary. ReqId: " + reqId + ", Acct: " + account + ", Tag: " + tag + ", Value: " + value
                + ", Currency: " + currency);
    }
    // ! [accountsummary]

    // ! [accountsummaryend]
    @Override
    public void accountSummaryEnd(int reqId) {
        log.info("AccountSummaryEnd. Req Id: " + reqId + "\n");
    }

    // ! [accountsummaryend]
    @Override
    public void verifyMessageAPI(String apiData) {
        log.info("verifyMessageAPI");
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
        log.info("verifyCompleted");
    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {
        log.info("verifyAndAuthMessageAPI");
    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
        log.info("verifyAndAuthCompleted");
    }

    // ! [displaygrouplist]
    @Override
    public void displayGroupList(int reqId, String groups) {
        log.info("Display Group List. ReqId: " + reqId + ", Groups: " + groups + "\n");
    }
    // ! [displaygrouplist]

    // ! [displaygroupupdated]
    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
        log.info("Display Group Updated. ReqId: " + reqId + ", Contract info: " + contractInfo + "\n");
    }

    // ! [positionmulti]
 
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos,
            double avgCost) {
        log.info("Position Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode
                + ", Symbol: " + contract.symbol() + ", SecType: " + contract.secType() + ", Currency: "
                + contract.currency() + ", Position: " + pos + ", Avg cost: " + avgCost + "\n");
    }
    // ! [positionmulti]

    // ! [positionmultiend]
    @Override
    public void positionMultiEnd(int reqId) {
        log.info("Position Multi End. Request: " + reqId + "\n");
    }
    // ! [positionmultiend]

    // ! [accountupdatemulti]
    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value,
            String currency) {
        log.info("Account Update Multi. Request: " + reqId + ", Account: " + account + ", ModelCode: " + modelCode
                + ", Key: " + key + ", Value: " + value + ", Currency: " + currency + "\n");
    }
    // ! [accountupdatemulti]

    // ! [accountupdatemultiend]
    @Override
    public void accountUpdateMultiEnd(int reqId) {
        log.info("Account Update Multi End. Request: " + reqId + "\n");
    }
    // ! [accountupdatemultiend]

    // ! [securityDefinitionOptionParameter]
    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId,
            String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {

      //  ContractHolder underlyingContractHolder = contractRepository.findById(underlyingConId).orElseGet(() -> {
          //  TwsResultHolder<ContractHolder> holder = requestContractByConid(underlyingConId);
           // return holder.getResult();
      //  });

        for (Types.Right right : List.of(Types.Right.Call, Types.Right.Put)) {
            for (String expiration : expirations) {
                for (Double strike : strikes) {
                   // String optionSymbol = underlyingContractHolder.getContract().symbol() + " " + expiration + " " + right;
                   // Option option = new Option(optionSymbol, expiration, strike, right);
                   // underlyingContractHolder.getOptionChain().add(option);
                }
            }
        }

       // underlyingContractHolder.setOptionChainRequestId(reqId);
       // contractRepository.save(underlyingContractHolder);
    }
    // ! [securityDefinitionOptionParameter]

    // ! [securityDefinitionOptionParameterEnd]
    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {
      //  ContractHolder underlying = contractRepository.findContractHolderByOptionChainRequestId(reqId);
       // if (underlying != null && !CollectionUtils.isEmpty(underlying.getOptionChain())) {
       //     twsResultHandler.setResult(reqId, new TwsResultHolder<>(underlying.getOptionChain()));
      //  }
        log.debug("Option chain retrieved: {}");
    }
    // ! [securityDefinitionOptionParameterEnd]

    // ! [softDollarTiers]
    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
        for (SoftDollarTier tier : tiers) {
            log.info("tier: " + tier.toString() + ", ");
        }
    }
    // ! [softDollarTiers]

    // ! [familyCodes]
    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
        for (FamilyCode fc : familyCodes) {
            log.info("Family Code. AccountID: " + fc.accountID() + ", FamilyCode: " + fc.familyCodeStr());
        }
    }
    // ! [familyCodes]

    // ! [symbolSamples]
    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
        List<Contract> resultList = new ArrayList<>();
        for (ContractDescription cd : contractDescriptions) {
            resultList.add(cd.contract());
        }
      //  twsResultHandler.setResult(reqId, new TwsResultHolder(resultList));
    }
    // ! [symbolSamples]

    // ! [mktDepthExchanges]
    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
        for (DepthMktDataDescription depthMktDataDescription : depthMktDataDescriptions) {
            log.info("Depth Mkt Data Description. Exchange: " + depthMktDataDescription.exchange() +
                    ", ListingExch: " + depthMktDataDescription.listingExch() +
                    ", SecType: " + depthMktDataDescription.secType() +
                    ", ServiceDataType: " + depthMktDataDescription.serviceDataType() +
                    ", AggGroup: " + depthMktDataDescription.aggGroup());
        }
    }
    // ! [mktDepthExchanges]

    // ! [tickNews]
    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline,
            String extraData) {
        log.info("Tick News. TickerId: " + tickerId + ", TimeStamp: " + timeStamp + ", ProviderCode: " + providerCode
                + ", ArticleId: " + articleId + ", Headline: " + headline + ", ExtraData: " + extraData + "\n");
    }
    // ! [tickNews]

    // ! [smartcomponents]
    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {
        log.info("smart components req id:" + reqId);

        for (Map.Entry<Integer, Map.Entry<String, Character>> item : theMap.entrySet()) {
            log.info("bit number: " + item.getKey() +
                    ", exchange: " + item.getValue().getKey() + ", exchange letter: " + item.getValue().getValue());
        }
    }
    // ! [smartcomponents]

    // ! [tickReqParams]
    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
        log.info("Tick req params. Ticker Id:" + tickerId + ", Min tick: " + minTick + ", bbo exchange: " + bboExchange
                + ", Snapshot permissions: " + snapshotPermissions);
    }
    // ! [tickReqParams]

    // ! [newsProviders]
    @Override
    public void newsProviders(NewsProvider[] newsProviders) {
        for (NewsProvider np : newsProviders) {
            log.info("News Provider. ProviderCode: " + np.providerCode() + ", ProviderName: " + np.providerName()
                    + "\n");
        }
    }
    // ! [newsProviders]

    // ! [newsArticle]
    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {
        log.info("News Article. Request Id: " + requestId + ", ArticleType: " + articleType +
                ", ArticleText: " + articleText);
    }
    // ! [newsArticle]

    // ! [historicalNews]
    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
        log.info("Historical News. RequestId: " + requestId + ", Time: " + time + ", ProviderCode: " + providerCode
                + ", ArticleId: " + articleId + ", Headline: " + headline + "\n");
    }
    // ! [historicalNews]

    // ! [historicalNewsEnd]
    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {
        log.info("Historical News End. RequestId: " + requestId + ", HasMore: " + hasMore + "\n");
    }
    // ! [historicalNewsEnd]

    // ! [headTimestamp]
    @Override
    public void headTimestamp(int reqId, String headTimestamp) {
        log.info("Head timestamp. Req Id: " + reqId + ", headTimestamp: " + headTimestamp);
    }
    // ! [headTimestamp]

    // ! [histogramData]
    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {
        log.info(EWrapperMsgGenerator.histogramData(reqId, items));
    }
    // ! [histogramData]

    // ! [historicalDataUpdate]
    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {
        log.info("HistoricalDataUpdate. " + reqId + " - Date: " + bar.time() + ", Open: " + bar.open() + ", High: "
                + bar.high() + ", Low: " + bar.low() + ", Close: " + bar.close() + ", Volume: " + bar.volume()
                + ", Count: " + bar.count() + ", WAP: " + bar.wap());
    }
    // ! [historicalDataUpdate]

    // ! [rerouteMktDataReq]
    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {
        log.info(EWrapperMsgGenerator.rerouteMktDataReq(reqId, conId, exchange));
    }
    // ! [rerouteMktDataReq]

    // ! [rerouteMktDepthReq]
    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
        log.info(EWrapperMsgGenerator.rerouteMktDepthReq(reqId, conId, exchange));
    }
    // ! [rerouteMktDepthReq]

    // ! [marketRule]
    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(340);
        log.info("Market Rule Id: " + marketRuleId);
        for (PriceIncrement pi : priceIncrements) {
            log.info("Price Increment. Low Edge: " + df.format(pi.lowEdge()) + ", Increment: "
                    + df.format(pi.increment()));
        }
    }
    // ! [marketRule]

    // ! [pnl]
    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
        log.info(EWrapperMsgGenerator.pnl(reqId, dailyPnL, unrealizedPnL, realizedPnL));
    }
    // ! [pnl]

    // ! [pnlsingle]
    // public void pnlSingle(int reqId, Decimal pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
    //     log.info(EWrapperMsgGenerator.pnlSingle(reqId, pos, dailyPnL, unrealizedPnL, realizedPnL, value));
    // }
    // ! [pnlsingle]

    // ! [historicalticks]
    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
        for (HistoricalTick tick : ticks) {
            log.info(EWrapperMsgGenerator.historicalTick(reqId, tick.time(), tick.price(), tick.size()));
        }
    }
    // ! [historicalticks]

    // ! [historicalticksbidask]
    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
        for (HistoricalTickBidAsk tick : ticks) {
            log.info(EWrapperMsgGenerator.historicalTickBidAsk(reqId, tick.time(), tick.tickAttribBidAsk(),
                    tick.priceBid(), tick.priceAsk(), tick.sizeBid(),
                    tick.sizeAsk()));
        }
    }
    // ! [historicalticksbidask]

    @Override
    // ! [historicaltickslast]
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
        for (HistoricalTickLast tick : ticks) {
            log.info(EWrapperMsgGenerator.historicalTickLast(reqId, tick.time(), tick.tickAttribLast(), tick.price(),
                    tick.size(), tick.exchange(),
                    tick.specialConditions()));
        }
    }
    // ! [historicaltickslast]

    // ! [tickbytickalllast]

    // public void tickByTickAllLast(int reqId, int tickType, long time, double price, Decimal size,
    //         TickAttribLast tickAttribLast,
    //         String exchange, String specialConditions) {
    //     log.info(EWrapperMsgGenerator.tickByTickAllLast(reqId, tickType, time, price, size, tickAttribLast, exchange,
    //             specialConditions));
    // }
    // ! [tickbytickalllast]

    // ! [tickbytickbidask]

    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
            TickAttribBidAsk tickAttribBidAsk) {
        //timeSeriesHandler.addToStream(reqId, bidPrice, TickType.BID);
        //timeSeriesHandler.addToStream(reqId, askPrice, TickType.ASK);
    }
    // ! [tickbytickbidask]

    // ! [tickbytickmidpoint]
    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {
        log.info(EWrapperMsgGenerator.tickByTickMidPoint(reqId, time, midPoint));
    }
    // ! [tickbytickmidpoint]

    // ! [orderbound]
    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {
        log.info(EWrapperMsgGenerator.orderBound(orderId, apiClientId, apiOrderId));
    }
    // ! [orderbound]

    // ! [completedorder]
    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {
       
        String msg = EWrapperMsgGenerator.completedOrder(contract, order, orderState);
        pushBuySellSockMessage(msg); 
        log.info(msg);

    }
    // ! [completedorder]

    // ! [completedordersend]
    @Override
    public void completedOrdersEnd() {

        String msg = (EWrapperMsgGenerator.completedOrdersEnd());
        pushBuySellSockMessage(msg);
        log.info(msg);

    }
    // ! [completedordersend]

    // ! [displaygroupupdated]
    @Override
    public void error(Exception e) {
        log.error(e.getMessage());
        pushBuySellSockMessage(e.getMessage());
    }

    @Override
    public void error(String str) {
        String msg = EWrapperMsgGenerator.error(str) ;
        pushBuySellSockMessage(msg);
        log.error(str);
    }

    // ! [error]
    @Override
    public void error(int id, int errorCode, String errorMsg, String errcd) {
        String msg = EWrapperMsgGenerator.error(id, errorCode, errorMsg, errcd) ;
        pushBuySellSockMessage(msg);
        log.error("Error id: {}; Code: {}: {}", id, errorCode, errorMsg);
        //twsResultHandler.setResult(id, new TwsResultHolder("Error code: " + errorCode + "; " + errorMsg));
    }

    // ! [error]
    @Override
    public void connectionClosed() {
        log.info("Connection closed");
    }



    @Override
    public void historicalSchedule(int arg0, String arg1, String arg2, String arg3, List<HistoricalSession> arg4) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'historicalSchedule'");
    }

    @Override
    public void orderStatus( int orderId, String status, Decimal filled, Decimal remaining,
    double avgFillPrice, long permId, int parentId,
    double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
                // received order status
    	String msg = EWrapperMsgGenerator.orderStatus( orderId, status, filled, remaining,
        avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice);
        pushBuySellSockMessage(msg);
        log.info(msg);
    }



    @Override
    public void pnlSingle(int arg0, Decimal arg1, double arg2, double arg3, double arg4, double arg5) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pnlSingle'");
    }

    @Override
    public void position(String arg0, Contract arg1, Decimal arg2, double arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'position'");
    }

    @Override
    public void positionMulti(int arg0, String arg1, String arg2, Contract arg3, Decimal arg4, double arg5) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'positionMulti'");
    }

    @Override
    public void realtimeBar(int arg0, long arg1, double arg2, double arg3, double arg4, double arg5, Decimal arg6,
            Decimal arg7, int arg8) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'realtimeBar'");
    }

    @Override
    public void replaceFAEnd(int arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replaceFAEnd'");
    }

    @Override
    public void tickByTickAllLast(int arg0, int arg1, long arg2, double arg3, Decimal arg4, TickAttribLast arg5,
            String arg6, String arg7) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tickByTickAllLast'");
    }

    @Override
    public void tickByTickBidAsk(int arg0, long arg1, double arg2, double arg3, Decimal arg4, Decimal arg5,
            TickAttribBidAsk arg6) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tickByTickBidAsk'");
    }

    @Override
    public void tickOptionComputation(int arg0, int arg1, int arg2, double arg3, double arg4, double arg5, double arg6,
            double arg7, double arg8, double arg9, double arg10) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tickOptionComputation'");
    }



    @Override
    public void updateMktDepth(int arg0, int arg1, int arg2, int arg3, double arg4, Decimal arg5) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMktDepth'");
    }

    @Override
    public void updateMktDepthL2(int arg0, int arg1, String arg2, int arg3, int arg4, double arg5, Decimal arg6,
            boolean arg7) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMktDepthL2'");
    }

    @Override
    public void updatePortfolio(Contract arg0, Decimal arg1, double arg2, double arg3, double arg4, double arg5,
            double arg6, String arg7) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePortfolio'");
    }

    @Override
    public void userInfo(int arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'userInfo'");
    }

    @Override
    public void wshEventData(int arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'wshEventData'");
    }

    @Override
    public void wshMetaData(int arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'wshMetaData'");
    }

    
    
}
