package com.example.app.service;

import com.example.app.model.SeckillProduct;
import com.example.app.repository.SeckillProductRepository;
import com.example.app.dto.ProductRequest;
import com.example.app.exception.ProductException;
import com.example.app.exception.ProductNotFoundException;
import com.example.app.exception.ProductStringException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductService{
    private final SeckillProductRepository seckillProductRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    public ProductService(SeckillProductRepository seckillProductRepository){
        this.seckillProductRepository=seckillProductRepository;    
    }
    public List<SeckillProduct> getAllProducts() {
        return seckillProductRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
    public SeckillProduct getProductId(Long productId){
       SeckillProduct product=seckillProductRepository.findById(productId).orElse(null);
            return product;          
    }
    public SeckillProduct getProductName(String productName){
       SeckillProduct product=seckillProductRepository.findByName(productName);
            return product;          
    }
    
    public SeckillProduct createProduct(ProductRequest request){
        
        SeckillProduct existingProduct = seckillProductRepository.findByName(request.getName());
        if(existingProduct!=null)
        {
            throw new ProductException(request.getName(),existingProduct.getId());
        }
        validateActivityTime(request.getStartTime(), request.getEndTime());
        SeckillProduct seckillProduct = new SeckillProduct(request.getName(),request.getStock(),request.getPrice(),request.getStartTime(),request.getEndTime());
        
        seckillProductRepository.save(seckillProduct);
        log.info("商品数据创建成功,productId={}",seckillProduct.getId());
        return seckillProduct;
    }
    public SeckillProduct updateProduct(Long productId,ProductRequest request){
        SeckillProduct product =getProductId(productId);
        if(product==null){
            throw new ProductNotFoundException(productId);
        }
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (seckillProductRepository.existsByName(request.getName())) {
                throw new ProductStringException("商品名字重复了");
            }
        }
        LocalDateTime startTime = request.getStartTime() != null
                ? request.getStartTime()
                : product.getStartTime();
        LocalDateTime endTime = request.getEndTime() != null
                ? request.getEndTime()
                : product.getEndTime();
        validateActivityTime(startTime, endTime);
        if(request.getName()!=null){
            product.setName(request.getName());
        }
        if(request.getPrice()!=null){
            product.setPrice(request.getPrice());
        }
        if(request.getStock()!=null){
            product.setStock(request.getStock());
        }
        if(request.getStartTime()!=null){    
            product.setStartTime(request.getStartTime());
        }
        if(request.getEndTime()!=null){
            product.setEndTime(request.getEndTime());
        }
        seckillProductRepository.save(product);
        log.info("商品数据更新成功,productId={}",product.getId());
        return product;
    }

    private void validateActivityTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("活动开始时间必须早于结束时间");
        }
    }


}
