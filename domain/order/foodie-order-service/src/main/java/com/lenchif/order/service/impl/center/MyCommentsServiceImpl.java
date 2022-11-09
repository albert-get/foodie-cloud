package com.lenchif.order.service.impl.center;

import com.lenchif.enums.YesOrNo;
import com.lenchif.item.service.ItemCommentsService;
import com.lenchif.order.fallback.itemservice.ItemCommentsFeignClient;
import com.lenchif.order.mapper.OrderItemsMapper;
import com.lenchif.order.mapper.OrderStatusMapper;
import com.lenchif.order.mapper.OrdersMapper;
import com.lenchif.order.pojo.OrderItems;
import com.lenchif.order.pojo.OrderStatus;
import com.lenchif.order.pojo.Orders;
import com.lenchif.order.pojo.bo.center.OrderItemsCommentBO;
import com.lenchif.order.service.center.MyCommentsService;
import com.lenchif.service.BaseService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MyCommentsServiceImpl extends BaseService implements MyCommentsService {

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private Sid sid;

    @Autowired
//    private ItemCommentsService itemCommentsService;
    private ItemCommentsFeignClient itemCommentsService;

    // feign章节里改成item-api
//    @Autowired
//    private LoadBalancerClient client;
//    @Autowired
//    private RestTemplate restTemplate;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComment(String orderId) {

        OrderItems orderItems = new OrderItems();
        orderItems.setOrderId(orderId);
        return orderItemsMapper.select(orderItems);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId, List<OrderItemsCommentBO> commentList) {
        for(OrderItemsCommentBO oic:commentList){
            oic.setCommentId(sid.nextShort());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("commentList",commentList);
        itemCommentsService.saveComments(map);
//        ServiceInstance instance = client.choose("FOODIE-ITEM-SERVICE");
//        String url = String.format("http://%s:%s/item-comments-api/saveComments",
//                instance.getHost(),
//                instance.getPort());
//        //   偷个懒，不判断返回status，等下个章节用Feign重写
//        restTemplate.postForLocation(url, map);

        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setIsComment(YesOrNo.YES.type);

        ordersMapper.updateByPrimaryKeySelective(orders);

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }

     //  移到了itemCommentService

//    @Transactional(propagation = Propagation.SUPPORTS)
//    @Override
//    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("userId",userId);
//        PageHelper.startPage(page,pageSize);
//        List<MyCommentVO> list = itemsCommentsMapperCustom.queryMyComments(map);
//        return setterPagedGrid(list, page);
//    }
}
