package com.example.stock.stockdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockdataApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockdataApplication.class, args);
	}

}
