package com.example.stock.stockdata.repository;


import com.example.stock.stockdata.entity.IBDataEntity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface IBDataEntityRepository extends JpaRepository<IBDataEntity, Long> {
    // Inherits CRUD operations from JpaRepository

    @Query("SELECT j FROM IBDataEntity j WHERE j.dateTime > :dateTime AND j.name = :symbol ORDER BY j.dateTime ASC") 
    List<IBDataEntity> findByTimestampAfter(@Param("dateTime") LocalDateTime dateTime, 
    @Param("symbol") String symbol);


    
}