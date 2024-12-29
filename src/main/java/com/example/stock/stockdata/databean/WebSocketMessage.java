package com.example.stock.stockdata.databean;

public class WebSocketMessage {

    private String action; 
    private int tickerId; 
    private String symbol;
    private String backFillDuration;
    private String barSizeSetting;
    private String useRTH;
    private String keepUpToDate;
    private String orderSize;
    private String trailingPercent;

    
    public String getOrderSize() {
        return orderSize;
    }
    public void setOrderSize(String orderSize) {
        this.orderSize = orderSize;
    }
    public String getTrailingPercent() {
        return trailingPercent;
    }
    public void setTrailingPercent(String trailingPercent) {
        this.trailingPercent = trailingPercent;
    }
    public String getBackFillDuration() {
        return backFillDuration;
    }
    public void setBackFillDuration(String backFillDuration) {
        this.backFillDuration = backFillDuration;
    }
    public String getBarSizeSetting() {
        return barSizeSetting;
    }
    public void setBarSizeSetting(String barSizeSetting) {
        this.barSizeSetting = barSizeSetting;
    }
    public String getUseRTH() {
        return useRTH;
    }
    public void setUseRTH(String useRTH) {
        this.useRTH = useRTH;
    }
    public String getKeepUpToDate() {
        return keepUpToDate;
    }
    public void setKeepUpToDate(String keepUpToDate) {
        this.keepUpToDate = keepUpToDate;
    }
      
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
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
