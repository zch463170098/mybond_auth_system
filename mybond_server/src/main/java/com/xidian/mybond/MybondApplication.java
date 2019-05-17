package com.xidian.mybond;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MybondApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybondApplication.class, args);
        System.out.println("mybond is running");
    }

}
