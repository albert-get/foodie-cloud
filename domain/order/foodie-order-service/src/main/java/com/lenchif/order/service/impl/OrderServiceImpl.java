package com.lenchif.order.service.impl;

import com.lenchif.enums.OrderStatusEnum;
import com.lenchif.enums.YesOrNo;
import com.lenchif.item.pojo.Items;
import com.lenchif.item.pojo.ItemsSpec;
import com.lenchif.item.service.ItemService;
import com.lenchif.order.mapper.OrderItemsMapper;
import com.lenchif.order.mapper.OrderStatusMapper;
import com.lenchif.order.mapper.OrdersMapper;
import com.lenchif.order.pojo.OrderItems;
import com.lenchif.order.pojo.OrderStatus;
import com.lenchif.order.pojo.Orders;
import com.lenchif.order.pojo.bo.PlaceOrderBO;
import com.lenchif.order.pojo.bo.SubmitOrderBO;
import com.lenchif.order.pojo.vo.MerchantOrdersVO;
import com.lenchif.order.pojo.vo.OrderVO;
import com.lenchif.order.service.OrderService;
import com.lenchif.pojo.ShopcartBO;
import com.lenchif.user.pojo.UserAddress;
import com.lenchif.user.service.AddressService;
import com.lenchif.utils.DateUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private AddressService addressService;

    @Autowired
    private ItemService itemService;

//    @Autowired
//    private LoadBalancerClient client;
//
//    @Autowired
//    private RestTemplate restTemplate;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private Sid sid;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrderBO placeOrderBO) {

        List<ShopcartBO> shopcartList = placeOrderBO.getItems();
        SubmitOrderBO submitOrderBO = placeOrderBO.getOrder();

        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        Integer postAmount=0;

        String orderId = sid.nextShort();

        UserAddress userAddress = addressService.queryUserAddres(userId, addressId);

//        ServiceInstance instance = client.choose("FOODIE-USER-SERVICE");
//        String url = String.format("http://%s:%s/address-api/queryAddress" +
//                        "?userId=%s&addressId=%s",
//                instance.getHost(),
//                instance.getPort(),
//                userId, addressId);
//        UserAddress userAddress = restTemplate.getForObject(url, UserAddress.class);

        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);

        newOrder.setReceiverName(userAddress.getReceiver());
        newOrder.setReceiverMobile(userAddress.getMobile());
        newOrder.setReceiverAddress(
                userAddress.getProvince()+" "
                +userAddress.getCity()+" "
                +userAddress.getDistrict()+ " "
                +userAddress.getDetail()
        );

        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);
        newOrder.setPostAmount(postAmount);

        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());

        String[] specList = itemSpecIds.split(",");
        Integer totalAmount=0;
        Integer realPayAmount=0;
        List<ShopcartBO> toBeRemovedShopcatdList = new ArrayList<>();
        for(String itemSpecId:specList){
            //  整合redis后，商品购买的数量重新从redis的购物车中获取

            ShopcartBO cartItem = getBuyCountsFromShopcart(shopcartList, itemSpecId);
            int buyCounts = cartItem.getBuyCounts();
            toBeRemovedShopcatdList.add(cartItem);

//            int buyCounts=1;
            ItemsSpec itemsSpec = itemService.queryItemSpecById(itemSpecId);
//            ServiceInstance itemApi = client.choose("FOODIE-ITEM-SERVICE");
//            url = String.format("http://%s:%s/item-api/singleItemSpec?specId=%s",
//                    itemApi.getHost(),
//                    itemApi.getPort(),
//                    itemSpecId);
//            ItemsSpec itemsSpec = restTemplate.getForObject(url, ItemsSpec.class);
            totalAmount+=itemsSpec.getPriceNormal()*buyCounts;
            realPayAmount+=itemsSpec.getPriceDiscount()*buyCounts;

            String itemId = itemsSpec.getItemId();
            Items item = itemService.queryItemById(itemId);
//            url = String.format("http://%s:%s/item-api/item?itemId=%s",
//                    itemApi.getHost(),
//                    itemApi.getPort(),
//                    itemId);
//            Items item = restTemplate.getForObject(url, Items.class);
            String imgUrl = itemService.queryItemMainImgById(itemId);
//            url = String.format("http://%s:%s/item-api/primaryImage?itemId=%s",
//                    itemApi.getHost(),
//                    itemApi.getPort(),
//                    itemId);
//            String imgUrl = restTemplate.getForObject(url, String.class);
            String subOrderId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemName(item.getItemName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemsSpec.getName());
            subOrderItem.setPrice(itemsSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);

            itemService.decreaseItemSpecStock(itemSpecId,buyCounts);
//            url = String.format("http://%s:%s/item-api/decreaseStock?specId=%s&buyCounts=%s",
//                    itemApi.getHost(),
//                    itemApi.getPort(),
//                    itemSpecId,buyCounts);
//            restTemplate.postForLocation(url,new Object());
        }

        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAmount);
        ordersMapper.insert(newOrder);

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        orderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(orderStatus);

        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAmount+postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);

        return orderVO;

    }

    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartList, String specId) {
        for (ShopcartBO cart : shopcartList) {
            if (cart.getSpecId().equals(specId)) {
                return cart;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus orderStatusPojo = new OrderStatus();
        orderStatusPojo.setOrderId(orderId);
        orderStatusPojo.setOrderStatus(orderStatus);
        orderStatusPojo.setPayTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(orderStatusPojo);
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder() {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(orderStatus);
        for(OrderStatus os:list){
            Date createdTime = os.getCreatedTime();
            int daysBetween = DateUtil.daysBetween(createdTime, new Date());
            if(daysBetween>=1){
                doCloseOrder(os.getOrderId());
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    void doCloseOrder(String orderId){
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setOrderStatus(OrderStatusEnum.CLOSE.type);
        orderStatus.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }
}
