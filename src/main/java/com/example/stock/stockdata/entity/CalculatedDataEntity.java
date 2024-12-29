package com.example.stock.stockdata.entity;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "calculateddata")
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown JSON properties
@AllArgsConstructor
@NoArgsConstructor
public class CalculatedDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // symbol

    private int minCount; //number of minutes

    private LocalDateTime dateTime; // data time

    private Double volume;

    private Double price;

    private Double rsi;

    private String note;

}
