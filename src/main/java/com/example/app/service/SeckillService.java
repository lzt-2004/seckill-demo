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
import java.util.List;
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
    private Long deductStockLua(String stockKey,String buyKey,String username){
            return redisTemplate.execute(STOCK_SCRIPT,Arrays.asList(stockKey, buyKey),username);
        }
    private Long rollbackStockLua(String stockKey,String buyKey,String username){
            return redisTemplate.execute(STOCK_SCRIPTTWO,Arrays.asList(stockKey, buyKey),username);
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
        Long result = deductStockLua(stockKey, buyKey, username);
        
        if (result == null) {
            throw new SeckillException("Lua 执行异常");
        }

        if (result == 2L) {
            return "你已购买";
        }

        if (result == 0L) {
            return "库存不够";
        }

        if (result != 1L) {
            throw new SeckillException("Lua 返回未知结果");
        }
        try{    
            OrderStatus status = OrderStatus.PENDING;
            LocalDateTime createTime = now;
            LocalDateTime expireTime = now.plusMinutes(10);

            SeckillOrder seckillOrder = new SeckillOrder(username, product, status, createTime, expireTime);
            seckillOrderRepository.saveAndFlush(seckillOrder);
            log.info("用户抢购成功数据已保存,username={}, productId={}, orderId={}",username, productId, seckillOrder.getId());
            return "抢到了，订单号：" + seckillOrder.getId();
        }catch(Exception exception){
            try {
                Long rollbackResult = rollbackStockLua(stockKey,buyKey, username);
                log.error("订单创建失败，已尝试补偿 Redis 资格，username={}, productId={}, rollbackResult={}",username, productId, rollbackResult, exception);
            } catch (Exception rollbackException) {
                log.error("订单创建失败且 Redis 补偿失败，需要人工核对，username={}, productId={}",username, productId, rollbackException);
            }

            throw new SeckillException("创建订单失败，请稍后重试");
        }
        
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
    public List<SeckillOrder> getMyOrders(){
        String username = getCurrentUsername();
        return seckillOrderRepository.findByUsernameOrderByCreateTimeDesc(username);
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
            Long resultTwo = rollbackStockLua(stockKey, buyKey, username);

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
        @Transactional
        public SeckillOrder cancelOrder(Long orderId) {
            SeckillOrder order = getOrderById(orderId);

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new SeckillException("当前订单不能取消");
            }

            int rows = seckillOrderRepository.cancelIfPending(orderId);
            if (rows == 0) {
                throw new SeckillException("订单状态已变化，取消失败");
            }

            Long productId = order.getProduct().getId();
            String stockKey = "stock:" + productId;
            String buyKey = "seckill:users:" + productId;
            Long rollbackResult = rollbackStockLua(stockKey,buyKey,order.getUsername());
            if (rollbackResult == null || rollbackResult != 1L) {
                throw new SeckillException("取消订单失败，释放抢购资格失败");
            }
            order.setStatus(OrderStatus.CANCELLED);
            log.info("用户主动取消订单成功，orderId={}, username={}, productId={}",orderId, order.getUsername(), productId);
            return order;
        }
    }



    

