package com.bigbird.netty.httpdemo4;

import java.util.UUID;

public class OrderService {
    public QueryOrderInfoRes queryOrderInfo(QueryOrderInfoReq req) {
        QueryOrderInfoRes res = new QueryOrderInfoRes();
        Order order = new Order();
        order.setOrderId(req.getOrderId());
        order.setOrderAmount(19999);
        order.setProductId(UUID.randomUUID().toString());
        order.setUserId(UUID.randomUUID().toString().replace("-", "").toUpperCase());
        res.setOrder(order);
        res.setRetMsg("success");
        return res;
    }
}
