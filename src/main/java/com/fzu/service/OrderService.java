package com.fzu.service;

import com.spring.*;

/**
 * @ClassName : UserService
 * @Description :
 * @Author : cybersa
 * @Date: 2020-08-11 17:57
 */
@Component(value = "orderService")
public class OrderService implements InitializingBean, BeanNameAware {

    @Autowired
    private UserService userService;

    private String beanName;
    public void test() {
        System.out.println(userService);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("初始化");
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
