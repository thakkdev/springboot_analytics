package com.example.stock.stockdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.stock.stockdata.EwrapperImpl;
import com.example.stock.stockdata.TWS;

@Configuration
public class AppConfig {
    @Bean
    public EwrapperImpl eWrapperImpl() {
        return new EwrapperImpl();
    }

    @Bean
    public TWS tws() {
        return new TWS();
    }


    /*
    @Bean public EClientSocket eClientSocket(EwrapperImpl ewrapperImpl) 
    {
         return ewrapperImpl.getClientSocket();
    }
          */
}

