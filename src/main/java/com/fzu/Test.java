package com.fzu;

import com.fzu.service.OrderService;
import com.spring.CyApplicationContext;

/**
 * @ClassName : Test
 * @Description :
 * @Author : cybersa
 * @Date: 2020-08-11 17:53
 */
public class Test {
    public static void main(String[] args) {
        CyApplicationContext applicationContext = new CyApplicationContext(AppConfig.class);

        OrderService orderService = (OrderService)applicationContext.getBean("orderService");
        System.out.println(orderService);
        orderService.test();
    }
}
