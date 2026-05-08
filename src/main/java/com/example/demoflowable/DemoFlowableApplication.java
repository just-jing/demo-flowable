package com.example.demoflowable;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {})
@MapperScan("com.example.demoflowable.exporttask.mapper")
public class DemoFlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoFlowableApplication.class, args);
    }

}
