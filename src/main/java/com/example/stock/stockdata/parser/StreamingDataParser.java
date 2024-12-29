package com.example.stock.stockdata.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamingDataParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Map<String, Object> jsonObject = new HashMap<String, Object>();

    public static boolean parseMessagesToJson(String message, Map<String, Object> injObject ) {

            String[] parts = message.split("\\s+");

            boolean startNewObject = false;

            // Iterate through the parts to populate the JSON object
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];

                    // Convert numeric values to numbers
                    if (value.matches("\\d+")) {
                        injObject.put(key, Integer.parseInt(value));
                    } else if (value.matches("\\d+\\.\\d+")) {
                        injObject.put(key, Double.parseDouble(value));
                    } else {
                        injObject.put(key, value);
                    }
                }
                if(keyValue.length == 2 && keyValue[0].contains("delayedLastTimestamp"))
                {
                    startNewObject = true;
                }
            }
           
        return startNewObject;
    }


    // public static void main(String[] args) {

    //     //Map<String, Object> jsonObject = new HashMap<String, Object>();

    //     String message1 =  "id=1 delayedLastTimestamp=1734707149";
    //     String message2 = "id=1 delayedHigh=513.28 noAutoExecute pastLimit=false";
    //     String message3 = "id=1 delayedVolume=97168";
    //     String message4 = "id=1 delayedBid=513.17 noAutoExecute pastLimit=false";
    //     String message5 = "id=1 delayedBidSize=3300";
    //     String message6 = "id=1 delayedLastTimestamp=1734707159";
    //     String message7 = "id=1 delayedVolume=97784";
    //     String message8 = "id=1 delayedAskSize=700";
    //     String message9 = "id=1 delayedHigh=513.35 noAutoExecute pastLimit=false"; 
    //     String message10 = "id=1 delayedVolume=97784";
    //     String message13 = "id=1 delayedAskSize=600";
    //     String message11 = "id=1 delayedAskSize=100";
    //     String message12 = "id=1 delayedLastTimestamp=1734707169";

    //     // Store messages in an array 
    //     String[] messages = 
    //     { message1, message2, message3, message4, message5, message6, message7, 
    //         message8, message9, message10, message13, message11, message12 };

    //     for (String msg : messages) 
    //     {
    //         boolean startNewObject = parseMessagesToJson(msg, jsonObject);
    //             if (startNewObject)
    //             {
    //                 //create a JSON message and push to websocket
    //                 try {
    //                    String jsonMessage = objectMapper.writeValueAsString(jsonObject);
    //                    System.out.println("Message is : " + jsonMessage);
    //                     //twsServiceWrapper.sendMessage(jsonMessage);
    //                 } catch (JsonProcessingException e) {
    //                 System.out.println(e.getMessage());
    //                 }
                
    //                 //create new JSONObject
    //                 jsonObject = new HashMap<String, Object>();
            
    //              }
    //     }
    //}
    
}
