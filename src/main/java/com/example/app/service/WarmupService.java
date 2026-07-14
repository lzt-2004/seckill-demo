package com.example.app.service;

import com.example.app.repository.SeckillProductRepository;
import com.example.app.model.SeckillProduct;
import com.example.app.exception.ProductStringException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WarmupService {
    private static final Logger log = LoggerFactory.getLogger(WarmupService.class);
    private final SeckillProductRepository seckillProductRepository;
    private final StringRedisTemplate redisTemplate;
    public WarmupService(SeckillProductRepository seckillProductRepository,StringRedisTemplate redisTemplate){
        this.seckillProductRepository=seckillProductRepository;
        this.redisTemplate=redisTemplate;
    }

    public SeckillProduct updateStock(Long productId){
        SeckillProduct product = seckillProductRepository.findById(productId).orElse(null);
        if(product==null){
            throw new ProductStringException("商品id不存在");
        }
        String cacheKey = "stock:"+productId;
        redisTemplate.opsForValue().set(cacheKey,product.getStock().toString());
        log.info("商品同步缓存成功,productId={},productName={}",productId,product.getName());
        return product;
    }
    public SeckillProduct resetStock(Long productId){
        SeckillProduct product = seckillProductRepository.findById(productId).orElse(null);
        if(product==null){
            throw new ProductStringException("商品id不存在");
        }
        String cacheKey = "stock:"+productId;
        String buyKey="seckill:users:"+productId;
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(buyKey);
        redisTemplate.opsForValue().set(cacheKey,product.getStock().toString());
        log.info("商品缓存重置成功,productId={},productName={}",productId,product.getName());
        return product;
    }


    
}
