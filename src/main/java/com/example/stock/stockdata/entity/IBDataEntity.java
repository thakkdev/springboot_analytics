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

/**
 * Entity class representing a rawintdata.
 */
@Data
@Entity
@Table(name = "rawintdata")
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown JSON properties
@AllArgsConstructor
@NoArgsConstructor
public class IBDataEntity {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name; // symbol

        private LocalDateTime dateTime; // datetime
    
        @Column(columnDefinition = "jsonb")
        @JdbcTypeCode(SqlTypes.JSON)
        private JsonNode jsonData;
    
    }
    
