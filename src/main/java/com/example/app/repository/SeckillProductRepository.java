package com.example.app.repository;

import com.example.app.model.SeckillProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface SeckillProductRepository extends JpaRepository<SeckillProduct,Long>{
    SeckillProduct findByName(String name);
    boolean existsByName(String name);
    void deleteByName(String name);
}
