package com.example.app.service;
import com.example.app.model.Todo;
import com.example.app.model.User;
import com.example.app.repository.TodoRepository;
import com.example.app.repository.JpaUserRepository;
import org.springframework.stereotype.Service;
import com.example.app.exception.TodoNotFoundException;
import com.example.app.exception.UnauthorizedAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collections;
import java.util.UUID;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class TodoService {
    private final TodoRepository todoRepository;
    private final JpaUserRepository jpaUserRepository;
    private final StringRedisTemplate redisTemplate; 
    private static final Logger log = LoggerFactory.getLogger(TodoService.class);
  
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setScriptText(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end"
        );
        UNLOCK_SCRIPT.setResultType(Long.class);
}

    public TodoService(TodoRepository todoRepository,JpaUserRepository jpaUserRepository,StringRedisTemplate redisTemplate){
        this.todoRepository=todoRepository;
        this.jpaUserRepository=jpaUserRepository;
        this.redisTemplate = redisTemplate;
    }
    @Transactional
    public Todo createTodo(String title,String content){
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();   
        User user = jpaUserRepository.findByUsername(username);
        Todo todo = new Todo(title, content, user);
        Todo savedTodo = todoRepository.save(todo);
        String cacheKey = "todos:" + username;
        redisTemplate.delete(cacheKey);
        return savedTodo;
    }
   public List<Todo> getMyTodos() {
    String username = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    String cacheKey = "todos:" + username;
    String cachedJson = redisTemplate.opsForValue().get(cacheKey);
    if (cachedJson != null) {
        log.debug("从 Redis 缓存获取待办列表,username={}",username);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(cachedJson, new TypeReference<List<Todo>>() {});
        } catch (Exception e) {
            redisTemplate.delete(cacheKey);
        }
    }
    log.debug("从数据库查询待办列表,username={}",username);
    User user = jpaUserRepository.findByUsername(username);
    List<Todo> todos = todoRepository.findByUserId(user.getId());
    try {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(todos);
        int expireMinutes = 10 + ThreadLocalRandom.current().nextInt(1, 6);
        redisTemplate.opsForValue().set(cacheKey, json,expireMinutes , TimeUnit.MINUTES);
    } catch (Exception e) {
        log.error("缓存写入失败：" , e);
    }
    return todos;
}
    public Todo getTodoById(Long id) {
    String currentUser = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    String cacheKey = "todo:" + currentUser + ":" + id;
    String cachedJson = redisTemplate.opsForValue().get(cacheKey);
    if (cachedJson != null) {
        if ("null".equals(cachedJson)) {
            log.debug("从 Redis 缓存获取单个待办, id={}" , id);
            throw new TodoNotFoundException(id);
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            log.debug("从 Redis 缓存获取单个待办, id={}" , id);
            return mapper.readValue(cachedJson, Todo.class);
        } catch (Exception e) {
            redisTemplate.delete(cacheKey);
        }
    }
    String lockKey = "lock:todo:" + currentUser + ":" + id;
    String lockValue = UUID.randomUUID().toString();
    Boolean locked = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS);
    if (Boolean.TRUE.equals(locked)) {
        try {
            String retryJson = redisTemplate.opsForValue().get(cacheKey);
            if (retryJson != null) {
                if ("null".equals(retryJson)) {
                    throw new TodoNotFoundException(id);
                }
                try{
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    log.debug("从 Redis 缓存获取单个待办, id={}" , id);
                    return mapper.readValue(retryJson, Todo.class);
                }
                catch(Exception e){
                    redisTemplate.delete(cacheKey);
                }
            }
            log.debug("从数据库查询单个待办,username={},todoId={}",currentUser,id);
            Todo todo = todoRepository.findById(id)
                .orElse(null);
            if (todo == null) {
                redisTemplate.opsForValue().set(cacheKey, "null", 2, TimeUnit.MINUTES);
                throw new TodoNotFoundException(id);
            }
            if (!todo.getUser().getUsername().equals(currentUser)) {
                throw new UnauthorizedAccessException("无权查看别人的待办");
            }
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                String json = mapper.writeValueAsString(todo);
                int expireMinutes = 10 + ThreadLocalRandom.current().nextInt(1, 6);
                redisTemplate.opsForValue().set(cacheKey, json, expireMinutes, TimeUnit.MINUTES);
            } catch (Exception e)
            {
                log.error("单个 Todo 缓存写入失败",e);
            }
            return todo;
        } 
        finally 
        {
            redisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(lockKey),
                lockValue
        );
        } 
    }
    try {
        Thread.sleep(100);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    String retryJson = redisTemplate.opsForValue().get(cacheKey);
    if (retryJson != null) {
        if ("null".equals(retryJson)) {
            log.debug("从 Redis 缓存获取单个待办, id={}",id);
            throw new TodoNotFoundException(id);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            log.debug("从 Redis 缓存获取单个待办, id={}",id);
            return mapper.readValue(retryJson, Todo.class);
        } catch (Exception e) {
            redisTemplate.delete(cacheKey);
        }
    }

    throw new RuntimeException("系统繁忙，请稍后重试");
    }
    


    @Transactional
    public Todo updateTodo(Long id, String title, String content, Boolean completed) {
   
    Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new TodoNotFoundException(id));
    
    String currentUser = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    if (!todo.getUser().getUsername().equals(currentUser)) {
        throw new UnauthorizedAccessException("无权操作别人的待办");
    }
    
   
    if (title != null) {
        todo.setTitle(title);
    }
    if (content != null) {
        todo.setContent(content);
    }
    if (completed != null) {
        todo.setCompleted(completed);
    }
   
    String listCacheKey = "todos:" + currentUser;
    String todoCacheKey = "todo:" + currentUser+":"+id;
    redisTemplate.delete(listCacheKey);
    redisTemplate.delete(todoCacheKey);
    return todoRepository.save(todo);
}
@Transactional
public void deleteTodo(Long id){
    Todo todo = todoRepository.findById(id)
            .orElseThrow(() -> new TodoNotFoundException(id));
    String currentUser = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    if (!todo.getUser().getUsername().equals(currentUser)) {
        throw new UnauthorizedAccessException("无权操作别人的待办");
    }
    todoRepository.delete(todo);
    String listCacheKey = "todos:" + currentUser;
    String todoCacheKey = "todo:" + currentUser+":"+id;
    redisTemplate.delete(listCacheKey);
    redisTemplate.delete(todoCacheKey);



}

    



}
