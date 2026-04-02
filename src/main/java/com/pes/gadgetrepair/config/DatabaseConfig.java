package com.pes.gadgetrepair.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/*
 Design Principles:
 1. Single Responsibility Principle
 2. Separation of Concerns

 Purpose:
 Explicitly enables JPA repositories and database configuration.
*/

@Configuration
@EnableJpaRepositories(basePackages = "com.pes.gadgetrepair.repository")
public class DatabaseConfig {
}