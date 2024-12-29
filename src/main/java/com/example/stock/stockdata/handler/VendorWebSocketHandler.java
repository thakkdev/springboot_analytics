package com.example.stock.stockdata.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ib.client.Contract;
import com.ib.client.EReader;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.stock.stockdata.databean.WebSocketMessage;
import com.example.stock.stockdata.service.AnalyticsService;
import com.example.stock.stockdata.service.TWSService;
import com.example.stock.stockdata.handler.HandleActions;

@Component
public class VendorWebSocketHandler extends TextWebSocketHandler {

    private final TWSService twsService;

    private final AnalyticsService analyticsService;

    private final HandleActions handActions = new HandleActions();

    private final ObjectMapper objectMapper = new ObjectMapper();

    //private WebSocketSession clientSession;

    // Map to hold client ID to WebSocketSession mapping 
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    //@Autowired
    public VendorWebSocketHandler(TWSService twsService, AnalyticsService analyticsService) {
        this.twsService = twsService;
        this.analyticsService = analyticsService;
        twsService.setWebSocketHandler(this);
        analyticsService.setWebSocketHandler(this);
        
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected to vendor WebSocket service");
        String clientId = getClientIdFromSession(session); 
        sessions.put(clientId,session);
        // Perform any additional setup or authentication here
        if(!twsService.isConnectedToTWS())
        {
            twsService.connectToTWS("127.0.0.1", 4002, 1);
        }
      // this.clientSession = session;
    }

    // Utility method to extract client ID from session 
    private String getClientIdFromSession(WebSocketSession session) 
    { 
        String query = session.getUri().getQuery(); 
        String clientId = null;
        if (query != null && query.contains("connectClientId")) 
        { 
            clientId = query.split("connectClientId=")[1];
        }
        return clientId;
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received message: " + message.getPayload());
        // Process the message received from the vendor service
        // Parse the incoming message 
        WebSocketMessage webSocketMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class); 
        
        handActions.messageTWSServiceActions(webSocketMessage, twsService, analyticsService);
    }

    public void sendMessage(String clientId, String message) throws IOException 
    { 
        WebSocketSession session = sessions.get(clientId);
        if (session != null && session.isOpen()) 
        { 
            session.sendMessage(new TextMessage(message)); 
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("WebSocket connection error: " + exception.getMessage());
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        
            String clientId = getClientIdFromSession(session); 
            sessions.remove(clientId); 
            System.out.println("WebSocket connection closed: " + status.getReason());

            if("rawdatapage".equals(clientId))
            {
                twsService.disconnectToTWS("True");
            }
    }
}

