package com.bms.project;

import com.bms.project.spring.CustomSpringContext;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author liqiang
 */
@EnableAsync
@MapperScan("com.bms.**.mapper")
@SpringBootApplication(scanBasePackages = {"com.bms"})
@Slf4j
public class AdminApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AdminApplication.class, args);
        CustomSpringContext.setApplicationContext(context);
        log.info("-------------------Springboot应用启动成功-------------------");
    }
}
