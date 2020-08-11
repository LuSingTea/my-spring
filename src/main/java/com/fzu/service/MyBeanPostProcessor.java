package com.fzu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

// 告诉Spring这个组件
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前" + beanName);
        return bean;
    }
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后" + beanName);
        return bean;
    }
}
