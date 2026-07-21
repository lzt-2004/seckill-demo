package com.example.app.repository;

import com.example.app.model.SeckillProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



@Repository
public interface SeckillProductRepository extends JpaRepository<SeckillProduct,Long>{
    SeckillProduct findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);

    @Modifying
    @Query("UPDATE SeckillProduct p SET p.stock = p.stock - 1 WHERE p.id = :productId AND p.stock > 0")
    int deductStock(@Param("productId") Long productId);
}
