package com.example.stock.stockdata.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.DependsOn;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.stockdata.databean.ConnectRequest;
import com.example.stock.stockdata.databean.DisconnectRequest;
import com.example.stock.stockdata.databean.MarketDataRequest;
import com.example.stock.stockdata.service.TWSService;
import com.ib.client.Contract;

@RestController
//DependsOn("tws")
@RequestMapping("/api")
public class TWSController {
    private final TWSService twsService;

    //@Autowired
    public TWSController(TWSService twsService) {
        this.twsService = twsService;
    }

    /* 
    @GetMapping("/connect")
    public String connect(@RequestParam String host, @RequestParam int port, @RequestParam int clientId) {
        twsService.connectToTWS(host, port, clientId);
        return "Connected to TWS";
    }
    */
    @PostMapping("/connect") 
    public String connect(@RequestBody ConnectRequest request) 
    { 
        return twsService.connectToTWS(request.getHost(), request.getPort(), request.getClientId()); 

    }

    @GetMapping("/search")
    String searchContract(@RequestParam String query) {
        return "Ho";
    }

    @PostMapping("/disconnect") 
    public String disconnect(@RequestBody DisconnectRequest request) 
    { 
        return twsService.disconnectToTWS(request.getDisconnectTWS()); 

    }

    @GetMapping("/marketData")
    public String requestMarketData(@RequestParam int tickerId, @RequestParam String symbol) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("STK");
        contract.currency("USD");
        contract.exchange("SMART");

        twsService.requestMarketData(tickerId, contract);
        return "Market data requested";
    }

    
 @PostMapping("/marketData") 
 public String requestMarketData(@RequestBody MarketDataRequest request) 
 { 
    Contract contract = new Contract(); 
    contract.symbol(request.getSymbol()); 
    contract.secType("STK"); 
    contract.currency("USD"); 
    contract.exchange("SMART"); 
    
    twsService.requestMarketData(request.getTickerId(), contract); 
    
    return "Market data requested";
 }
}

