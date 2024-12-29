package com.example.stock.stockdata.databean;

public class MarketDataRequest {
    private int tickerId;
    private String symbol;

    // Getters and setters
    public int getTickerId() {
        return tickerId;
    }

    public void setTickerId(int tickerId) {
        this.tickerId = tickerId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
