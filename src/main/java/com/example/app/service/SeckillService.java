package com.example.app.service;
import com.example.app.model.OrderStatus;
import com.example.app.model.SeckillOrder;
import com.example.app.model.SeckillProduct;
import com.example.app.repository.SeckillProductRepository;
import com.example.app.repository.SeckillOrderRepository;
import com.example.app.exception.SeckillException;

import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
@Service
public class SeckillService {
    private static final Logger log = LoggerFactory.getLogger(SeckillService.class);
    private final SeckillProductRepository seckillProductRepository;
    private final StringRedisTemplate redisTemplate;
    private final SeckillOrderRepository seckillOrderRepository;
    private static final DefaultRedisScript<Long> STOCK_SCRIPT,STOCK_SCRIPTTWO;
    static{
        STOCK_SCRIPT=new DefaultRedisScript<>();
        STOCK_SCRIPT.setScriptText(
    "local stockValue = redis.call('get', KEYS[1]) " +
    "if not stockValue then " +
    "   return 0 " +
    "end " +
    "local stock = tonumber(stockValue) " +
    "local seckill = redis.call('sismember', KEYS[2], ARGV[1]) " +
    "if seckill == 1 then " +
    "   return 2 " +
    "else " +
    "   if stock > 0 then " +
    "      redis.call('decr', KEYS[1]) " +
    "      redis.call('sadd', KEYS[2], ARGV[1]) " +
    "      return 1 " +
    "   else " +
    "      return 0 " +
    "   end " +
    "end"
    );
        STOCK_SCRIPT.setResultType(Long.class);
    }
    static{
        STOCK_SCRIPTTWO=new DefaultRedisScript<>();
        STOCK_SCRIPTTWO.setScriptText(
    "local stockValue = redis.call('get', KEYS[1]) " +
    "if not stockValue then " +
    "   return 0 " +
    "end " +
    "local seckill = redis.call('sismember', KEYS[2], ARGV[1]) " +
    "if seckill == 1 then " +
    "      redis.call('srem', KEYS[2], ARGV[1]) " +
    "      redis.call('incr', KEYS[1]) " +
    "   return 1 " +
    "else " +
    "   return 2 " +
    "end"
    );
        STOCK_SCRIPTTWO.setResultType(Long.class);
    }

    public  SeckillService(SeckillProductRepository seckillProductRepository,StringRedisTemplate redisTemplate,SeckillOrderRepository seckillOrderRepository )
    {
        this.seckillProductRepository=seckillProductRepository;
        this.redisTemplate=redisTemplate;
        this.seckillOrderRepository=seckillOrderRepository;
    }
    public SeckillProduct getProduct(Long productId){
       SeckillProduct product=seckillProductRepository.findById(productId).orElse(null);
            return product;          
    }
    private String getCurrentUsername(){
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
            || !authentication.isAuthenticated()
            || authentication.getName() == null
            || authentication.getName().isBlank()) {
            throw new SeckillException("用户未登录");
        }
        return authentication.getName();
    }
    

    public String seckill(Long productId) {
        SeckillProduct product = getProduct(productId);
        if (product == null) {
            return "商品不存在";
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(product.getStartTime())) {
            return "活动未开始";
        }
        if (now.isAfter(product.getEndTime())) {
            return "活动已结束";
        }
        String stockKey = "stock:" + productId;
        String buyKey = "seckill:users:" + productId;
        String username = getCurrentUsername();

        Long result = redisTemplate.execute(
                STOCK_SCRIPT,
                Arrays.asList(stockKey, buyKey),
                username
        );
        if (result != null && result == 2L) {
            log.warn("用户重复抢购username={}, productId={}", username, productId);
            return "你已购买";
        }

        if (result != null && result == 1L) {
            OrderStatus status = OrderStatus.PENDING;
            LocalDateTime createTime = now;
            LocalDateTime expireTime = now.plusMinutes(10);

            SeckillOrder seckillOrder = new SeckillOrder(
                    username, product, status, createTime, expireTime
            );
            seckillOrderRepository.save(seckillOrder);
            log.info("用户抢购成功数据已保存,username={}, productId={}, orderId={}",
                    username, productId, seckillOrder.getId());
            return "抢到了，订单号：" + seckillOrder.getId();
        }

        if (result != null && result == 0L) {
            log.warn("获取缓存资格失败库存不够,username={},productId={}",username,productId);
            return "库存不够";
        }
        log.warn("获取缓存资格失败lua执行异常,username={},productId={}",username,productId);
        throw new SeckillException( "lua执行异常");
    }
    public SeckillOrder getOrderById(Long orderId){
        SeckillOrder order = seckillOrderRepository.findById(orderId).orElse(null);
        if(order==null){
            throw new SeckillException("订单不存在");
        }
        String username =getCurrentUsername();
        if(!username.equals(order.getUsername())){
            throw new SeckillException("订单不存在");
        }
        log.info("订单查询成功,orderId={},username={}",orderId,username);   
        return order;
    }
    @Transactional
    public SeckillOrder payOrder(Long orderId) {
        SeckillOrder order = getOrderById(orderId);
        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new SeckillException("订单状态不是未支付状态");
        }
        SeckillOrder tempOrder = cancelExpiredOrder(order);
        if(tempOrder.getStatus().equals(OrderStatus.CANCELLED)){
            throw new SeckillException("订单是过期状态");
        }
        SeckillProduct product =order.getProduct();
        if(seckillOrderRepository.markPaidIfPending(orderId, LocalDateTime.now())==0){
                throw new SeckillException("支付失败");
            }
        if(seckillProductRepository.deductStock(product.getId())==0)
        {
            throw new SeckillException("库存不足");
        }else{
            log.info("订单支付成功,orderId={}, username={}, productId={}", order.getId(), order.getUsername(), product.getId());
            String buyKey = "seckill:users:" + product.getId();
            Long remove=redisTemplate.opsForSet().remove(buyKey,order.getUsername());
            if(remove!=null&&remove>0){
                log.info("限购标记删除成功,buyKey={},username={}",buyKey,order.getUsername()); 
            }else{
                log.warn("限购标记删除失败,buyKey={},username={}",buyKey,order.getUsername());
            }
            log.info("订单支付成功数据已更新保存,orderId={},username={}, productId={}", order.getId(), order.getUsername(), product.getId());
            
        }
        LocalDateTime payTime = LocalDateTime.now();
        order.setStatus(OrderStatus.PAID);
        order.setPayTime(payTime);
        return order;
    }
    @Transactional
    public SeckillOrder cancelExpiredOrder(SeckillOrder order){
        if(!order.getStatus().equals(OrderStatus.PENDING)){
            return order;
        }
        
        if (!LocalDateTime.now().isAfter(order.getExpireTime())) 
            {
                return order;
            } 
            Long productId = order.getProduct().getId();
            String username = order.getUsername();
            String stockKey = "stock:" + productId;
            String buyKey = "seckill:users:" + productId;

            if(seckillOrderRepository.cancelIfPending(order.getId())==0){
                log.info("订单已支付,不在执行本次取消订单任务username={},orderId={},productId={}",username,order.getId(),productId);
                return order;
            }
            Long resultTwo = redisTemplate.execute(
                STOCK_SCRIPTTWO,
                Arrays.asList(stockKey, buyKey),
                username
            );

            if (resultTwo == null) {
                log.warn("释放缓存资格失败回滚脚本执行失败,username={},orderId={},productId={}",username,order.getId(),productId);
                throw new SeckillException("回滚脚本执行失败");
            }
            if (resultTwo == 0L) {
                log.warn("释放缓存资格失败商品库存key不存在,username={},orderId={},productId={},",username,order.getId(),productId);
                throw new SeckillException("回滚失败库存key不存在");
            }
            if(resultTwo==1L){
                order.setStatus(OrderStatus.CANCELLED);
                log.info("释放资格成功订单状态已更新,username={},orderId={},productId={}",username,order.getId(),productId);
                return order;
            }
            if (resultTwo == 2L) {
                log.warn("释放缓存资格失败订单是待支付但是用户不在已抢订单名单内,orderId={}, username={}, productId={}", order.getId(), order.getUsername(), productId);
                return order;
            }
            log.warn("释放缓存资格失败未知异常");
            throw new SeckillException("出现异常了");
        }
    }



    

