package com.campus.literature;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园学术文献智能检索与综述生成系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.campus.literature.mapper")
public class CampusLiteratureApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusLiteratureApplication.class, args);
    }
}
