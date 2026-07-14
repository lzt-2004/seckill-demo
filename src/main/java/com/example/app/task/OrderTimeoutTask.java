package com.example.app.task;

import com.example.app.model.OrderStatus;
import com.example.app.model.SeckillOrder;
import com.example.app.repository.SeckillOrderRepository;
import com.example.app.service.SeckillService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTimeoutTask {
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);
    private final SeckillOrderRepository seckillOrderRepository;
    private final SeckillService seckillService;

    public OrderTimeoutTask(SeckillOrderRepository seckillOrderRepository,
                            SeckillService seckillService) {
        this.seckillOrderRepository = seckillOrderRepository;
        this.seckillService = seckillService;
    }

    @Scheduled(fixedRate = 300000)
    public void cancelExpiredOrders() {
        List<SeckillOrder> expiredOrders = seckillOrderRepository
                .findByStatusAndExpireTimeBefore(OrderStatus.PENDING, LocalDateTime.now());

        for (SeckillOrder order : expiredOrders) {
            seckillService.cancelExpiredOrder(order);
            
        }
        log.info("定时任务开始执行，过期订单数量={}", expiredOrders.size());

    }
}