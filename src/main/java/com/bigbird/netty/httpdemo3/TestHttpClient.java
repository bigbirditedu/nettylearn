package com.bigbird.netty.httpdemo3;

public class TestHttpClient {
    public static void main(String[] args) {
        CommonHttpClient commonHttpClient = new CommonHttpClient();
        commonHttpClient.init();
        HttpClientReq httpClientReq = new HttpClientReq("POST", "http://127.0.0.1:8899/orderService/queryOrderInfo");
        httpClientReq.setContent("{\"orderId\":\"121\"}");
        httpClientReq.setContentType("application/json");
        HttpClientRes res = commonHttpClient.call(httpClientReq);
        System.out.println(res.getContent());
        commonHttpClient.close();
    }
}
