package com.example.app.loadtest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SeckillLoadRunner {
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper objectMapper;

    public SeckillLoadRunner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Result run(Config config) throws Exception {
        List<TestUser> users = new ArrayList<>();
        for (int i = 0; i < config.userCount(); i++) {
            String username = "testUser_" + i;
            users.add(new TestUser(username, login(config, username)));
        }
        List<TestUser> requests = new ArrayList<>();
        for (TestUser user : users) {
            for (int i = 0; i < config.requestsPerUser(); i++) {
                requests.add(user);
            }
        }
        return runRequests(requests, config);
    }

    private String login(Config config, String username) throws Exception {
        String requestBody = "{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, config.testPassword());
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(config.baseUrl() + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = objectMapper.readTree(response.body());
        String token = body.path("data").asText();
        if (response.statusCode() != 200 || body.path("code").asInt(-1) != 0 || token.isBlank()) {
            throw new IllegalStateException("登录失败，username=" + username + "，response=" + response.body());
        }
        return token;
    }

    private Result runRequests(List<TestUser> requests, Config config) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(config.threadCount());
        int firstWave = Math.min(config.threadCount(), requests.size());
        CountDownLatch ready = new CountDownLatch(firstWave);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requests.size());
        Counter counter = new Counter();
        for (TestUser user : requests) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    counter.record(seckill(config, user));
                } catch (Exception ignored) {
                    counter.errorCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        long startTime = System.currentTimeMillis();
        start.countDown();
        done.await();
        long costMillis = System.currentTimeMillis() - startTime;
        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        return counter.toResult(requests.size(), costMillis);
    }

    private SeckillResult seckill(Config config, TestUser user) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.baseUrl() + "/api/seckill/" + config.productId()))
                .header("Authorization", "Bearer " + user.token())
                .POST(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode body = objectMapper.readTree(response.body());
        if (response.statusCode() == 200 && body.path("code").asInt(-1) == 0) {
            return SeckillResult.SUCCESS;
        }
        String message = body.path("message").asText();
        if ("库存不够".equals(message)) {
            return SeckillResult.OUT_OF_STOCK;
        }
        if ("你已购买".equals(message)) {
            return SeckillResult.DUPLICATE;
        }
        throw new IllegalStateException("抢购接口失败，username=" + user.username() + "，response=" + response.body());
    }

    public record Config(String baseUrl, String testPassword, long productId, int userCount, int requestsPerUser, int threadCount) { }
    public record Result(int totalRequests, int successCount, int outOfStockCount, int duplicateCount, int errorCount, long costMillis) { }
    private record TestUser(String username, String token) { }

    private enum SeckillResult {
        SUCCESS, OUT_OF_STOCK, DUPLICATE
    }

    private static class Counter {
        private final AtomicInteger successCount = new AtomicInteger();
        private final AtomicInteger outOfStockCount = new AtomicInteger();
        private final AtomicInteger duplicateCount = new AtomicInteger();
        private final AtomicInteger errorCount = new AtomicInteger();
        private void record(SeckillResult result) {
            switch (result) {
                case SUCCESS -> successCount.incrementAndGet();
                case OUT_OF_STOCK -> outOfStockCount.incrementAndGet();
                case DUPLICATE -> duplicateCount.incrementAndGet();
            }
        }
        private Result toResult(int total, long cost) {
            return new Result(total, successCount.get(), outOfStockCount.get(), duplicateCount.get(), errorCount.get(), cost);
        }
    }
}
