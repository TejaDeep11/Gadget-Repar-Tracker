package com.pes.gadgetrepair.config;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 Design Principles:
 1. Dependency Injection
 2. Open Closed Principle

 Design Pattern:
 Factory Pattern (FXMLLoader creation)

 Purpose:
 Allows Spring to inject dependencies into JavaFX controllers.
*/

@Configuration
public class JavaFxConfig {

    private final ApplicationContext applicationContext;

    public JavaFxConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public FXMLLoader fxmlLoader() {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(applicationContext::getBean);
        return loader;
    }
}