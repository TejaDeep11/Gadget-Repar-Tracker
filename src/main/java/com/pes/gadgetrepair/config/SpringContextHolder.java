package com.pes.gadgetrepair.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/*
 Design Principles Used:
 1. Dependency Injection (Spring)
 2. Inversion of Control (IoC)

 Design Pattern:
 Singleton Pattern (Spring manages single ApplicationContext instance)

 Purpose:
 Allows JavaFX controllers to access Spring-managed beans.
*/

@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}