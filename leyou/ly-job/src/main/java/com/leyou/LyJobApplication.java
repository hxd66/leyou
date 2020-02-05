package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   //开启springTask
public class LyJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(LyJobApplication.class, args);
    }
}