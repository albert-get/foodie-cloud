package com.lenchif.cart.controller;

import com.lenchif.cart.service.CartService;
import com.lenchif.controller.BaseController;
import com.lenchif.pojo.JSONResult;
import com.lenchif.pojo.ShopcartBO;
import com.lenchif.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "购物车接口controller", tags = {"购物车接口相关的api"})
@RequestMapping("shopcart")
@RestController
public class ShopcatController extends BaseController {

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private CartService cartService;

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public JSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        System.out.println(shopcartBO);

        cartService.addItemToCart(userId, shopcartBO);
        return JSONResult.ok();
    }

    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @PostMapping("/del")
    public JSONResult del(
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
            return JSONResult.errorMsg("参数不能为空");
        }

        cartService.removeItemFromCart(userId, itemSpecId);
        return JSONResult.ok();
    }

    //      1） 购物车清空功能
    //      2) 加减号 - 添加、减少商品数量
    //         +1 -1 -1 = 0  =>  -1 -1 +1 = 1 (问题： 如何保证前端请求顺序执行)

}
