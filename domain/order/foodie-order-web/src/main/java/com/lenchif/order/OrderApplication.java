package com.lenchif.order;

import com.lenchif.item.service.ItemService;
import com.lenchif.order.fallback.itemservice.ItemCommentsFeignClient;
import com.lenchif.user.service.AddressService;
import com.lenchif.user.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// 扫描 mybatis 通用 mapper 所在的包
@MapperScan(basePackages = "com.lenchif.order.mapper")
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.lenchif", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients(
        clients = {
                ItemCommentsFeignClient.class,
                ItemService.class,
                UserService.class,
                AddressService.class
        }
//        basePackages = {
//            "com.lenchif.user.service",
//            "com.lenchif.item.service",
//            "com.lenchif.order.fallback.itemservice"
//        }
)
@EnableCircuitBreaker
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
