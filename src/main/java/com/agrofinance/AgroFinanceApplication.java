package com.agrofinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the AgroFinance backend application.
 *
 * @SpringBootApplication is a meta-annotation combining:
 *  - @Configuration        : allows this class to declare beans
 *  - @EnableAutoConfiguration : auto-configures beans based on classpath contents
 *  - @ComponentScan        : scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class AgroFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgroFinanceApplication.class, args);
    }

}
