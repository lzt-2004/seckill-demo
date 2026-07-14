package com.example.app.repository;


import com.example.app.model.SeckillOrder;
import com.example.app.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;


public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {
    List<SeckillOrder> findByStatusAndExpireTimeBefore(OrderStatus status,LocalDateTime now);
}

