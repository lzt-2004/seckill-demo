package com.example.app.loadtest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SeckillLoadTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 4) throw new IllegalArgumentException("参数格式：<productId> <userCount> <requestsPerUser> <threadCount>");
        long productId = Long.parseLong(args[0]);
        int userCount = Integer.parseInt(args[1]);
        int requestsPerUser = Integer.parseInt(args[2]);
        int threadCount = Integer.parseInt(args[3]);
        if (productId <= 0 || userCount <= 0 || userCount > 100 || requestsPerUser <= 0 || threadCount <= 0) {
            throw new IllegalArgumentException("参数不合法：用户数范围为 1 到 100，其余参数必须大于 0");
        }
        SeckillLoadRunner.Result result = new SeckillLoadRunner(new ObjectMapper()).run(new SeckillLoadRunner.Config(
                "http://localhost:8080", "123456", productId, userCount, requestsPerUser, threadCount));
        System.out.println(result);
    }
}
