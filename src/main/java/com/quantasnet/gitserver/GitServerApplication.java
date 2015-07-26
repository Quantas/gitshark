package com.quantasnet.gitserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GitServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitServerApplication.class, args);
    }
}
