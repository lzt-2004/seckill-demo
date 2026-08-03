package com.example.app.repository;


import com.example.app.model.SeckillOrder;
import com.example.app.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;


public interface SeckillOrderRepository extends JpaRepository<SeckillOrder, Long> {
    List<SeckillOrder> findByStatusAndExpireTimeBefore(OrderStatus status,LocalDateTime now);
    List<SeckillOrder> findByUsernameOrderByCreateTimeDesc(String username);

    @Modifying
    @Query("UPDATE SeckillOrder o SET o.status = com.example.app.model.OrderStatus.PAID, o.payTime = :payTime WHERE o.id = :orderId AND o.status = com.example.app.model.OrderStatus.PENDING")
    int markPaidIfPending(@Param("orderId") Long orderId,
                      @Param("payTime") java.time.LocalDateTime payTime);
    @Modifying
    @Query("UPDATE SeckillOrder o SET o.status=com.example.app.model.OrderStatus.CANCELLED WHERE o.id=:orderId AND o.status=com.example.app.model.OrderStatus.PENDING")
    int cancelIfPending(@Param("orderId") Long orderId);

}

