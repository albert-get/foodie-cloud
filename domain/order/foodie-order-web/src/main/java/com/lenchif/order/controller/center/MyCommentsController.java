package com.lenchif.order.controller.center;


import com.lenchif.controller.BaseController;
import com.lenchif.enums.YesOrNo;
import com.lenchif.item.service.ItemCommentsService;
import com.lenchif.order.fallback.itemservice.ItemCommentsFeignClient;
import com.lenchif.order.pojo.OrderItems;
import com.lenchif.order.pojo.Orders;
import com.lenchif.order.pojo.bo.center.OrderItemsCommentBO;
import com.lenchif.order.service.center.MyCommentsService;
import com.lenchif.order.service.center.MyOrdersService;
import com.lenchif.pojo.JSONResult;
import com.lenchif.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "用户中心评价模块", tags = {"用户中心评价模块相关接口"})
@RestController
@RequestMapping("mycomments")
public class MyCommentsController extends BaseController {

    @Autowired
    private MyCommentsService myCommentsService;

    @Autowired
    private MyOrdersService myOrdersService;

    @Autowired
//    private ItemCommentsService itemCommentsService;
    private ItemCommentsFeignClient itemCommentsService;

//    @Autowired
//    private LoadBalancerClient client;
//
//    @Autowired
//    private RestTemplate restTemplate;

    @ApiOperation(value = "查询订单列表", notes = "查询订单列表", httpMethod = "POST")
    @PostMapping("/pending")
    public JSONResult pending(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId
    ){
        JSONResult result = myOrdersService.checkUserOrder(userId, orderId);
        if(result.getStatus()!= HttpStatus.OK.value()){
            return result;
        }
        Orders orders = (Orders) result.getData();
        if(orders.getIsComment()== YesOrNo.YES.type){
            return JSONResult.errorMsg("该笔订单已经评价");
        }
        List<OrderItems> list = myCommentsService.queryPendingComment(orderId);

        return JSONResult.ok(list);
    }

    @ApiOperation(value = "保存评论列表", notes = "保存评论列表", httpMethod = "POST")
    @PostMapping("/saveList")
    public JSONResult saveList(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @RequestParam String orderId,
            @RequestBody List<OrderItemsCommentBO> commentList
    ){
        JSONResult result = myOrdersService.checkUserOrder(userId, orderId);
        if(result.getStatus()!= HttpStatus.OK.value()){
            return result;
        }
        if(commentList==null||commentList.isEmpty()||commentList.size()==0){
            return JSONResult.errorMsg("评论内容不能为空");
        }
        myCommentsService.saveComments(userId,orderId,commentList);
        return JSONResult.ok();

    }

    @ApiOperation(value = "查询我的评价", notes = "查询我的评价", httpMethod = "POST")
    @PostMapping("/query")
    public JSONResult query(
            @ApiParam(name = "userId", value = "用户id", required = true)
            @RequestParam String userId,
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
            @RequestParam Integer pageSize
    ){
        if(StringUtils.isBlank(userId)){
            return JSONResult.errorMsg(null);
        }
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }


        PagedGridResult grid = itemCommentsService.queryMyComments(userId, page, pageSize);
        return JSONResult.ok(grid);

//        ServiceInstance instance = client.choose("FOODIE-ITEM-SERVICE");
//        String target = String.format("http://%s:%s/item-comments-api/myComments" +
//                        "?userId=%s&page=%s&pageSize=%s",
//                instance.getHost(),
//                instance.getPort(),
//                userId,
//                page,
//                pageSize);
//        // 偷个懒，不判断返回status，等下个章节用Feign重写
//        PagedGridResult grid = restTemplate.getForObject(target, PagedGridResult.class);
//        return JSONResult.ok(grid);

    }
}
