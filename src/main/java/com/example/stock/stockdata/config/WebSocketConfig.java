package com.example.stock.stockdata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.stock.stockdata.handler.VendorWebSocketHandler;
import com.example.stock.stockdata.service.AnalyticsService;
import com.example.stock.stockdata.service.TWSService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
   
    @Autowired 
    private ApplicationContext applicationContext;
    

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        TWSService twsService = applicationContext.getBean(TWSService.class);
        AnalyticsService analyticService = applicationContext.getBean(AnalyticsService.class);
        registry.addHandler(new VendorWebSocketHandler(twsService, analyticService), "/vendor-socket").setAllowedOrigins("*");
    }
}
