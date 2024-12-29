package com.example.stock.stockdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.data.jpa.repository.Query; 
import org.springframework.data.repository.query.Param;

import com.example.stock.stockdata.entity.CalculatedDataEntity;

public interface CalculatedDataEntityRepository extends JpaRepository<CalculatedDataEntity, Long> {

    @Query("SELECT c FROM CalculatedDataEntity c WHERE c.name = :name ") List<CalculatedDataEntity> 
    findByName(@Param("name") String name);

    @Query("SELECT c FROM CalculatedDataEntity c WHERE c.name = :name AND c.minCount = :minCount ") CalculatedDataEntity
    findByNameAndMinCount(@Param("name") String name, @Param("minCount") int minCount);

}
    

