package com.lenchif.controller;

import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class BaseController {

//    @Autowired
//    private MyOrdersService myOrdersService;

    public static final String FOODIE_SHOPCART = "shopcart";
    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final Integer PAGE_SIZE = 20;

    public String payReturnUrl = "http://api.z.mukewang.com/foodie-dev-api/orders/notifyMerchantOrderPaid";

    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";

    public static final String IMAGE_USER_FACE_LOCATION = File.separator+"Users" +
                                                            File.separator+"harry" +
            File.separator+"harry" +
            File.separator+"java_architect" +
            File.separator+"practice" +
            File.separator+"foodie" +
            File.separator+"foodie-api" +
            File.separator+"faces";

// FIXME 下面的逻辑移植到订单中心

//    public JSONResult checkUserOrder(String userId, String orderId){
//        Orders order = myOrdersService.queryMyOrder(userId, orderId);
//        if(order==null){
//            return JSONResult.errorMsg("订单不存在");
//        }
//        return JSONResult.ok(order);
//
//    }
}
