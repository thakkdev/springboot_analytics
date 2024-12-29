package com.example.stock.stockdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.stock.stockdata.service.GetTicketService;

@RestController
@RequestMapping("/api")
public class EwrapperController {
      
    private final GetTicketService ticketService;

    //@Autowired
    public EwrapperController(GetTicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/doSomething")
    public String doSomething() {
        //ticketService.performSomeOperation();
        return "Operation performed!";
    }
}
