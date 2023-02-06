package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/**
 * 问题解决 @MapperScan包引入错误导致的，切换为
 * import tk.mybatis.spring.annotation.MapperScan;
 * 可以正常获取到实体类
 */
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author word
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.imooc.mapper"})
//默认扫描子包和他的包的下所有的，
//这里添加自定义扫描
@ComponentScan(basePackages = {"com.imooc","org.n3r.idworker"})
@EnableMongoRepositories
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
