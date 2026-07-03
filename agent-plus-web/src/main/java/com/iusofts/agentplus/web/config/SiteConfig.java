
package com.iusofts.agentplus.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import(DatabaseConfig.class)
@ComponentScan(basePackages={"com.iusofts"})
@EnableScheduling
@Slf4j
public class SiteConfig implements CommandLineRunner {
    @Value("${spring.profiles.active}")
    private String profies;


    public void run(String... strings) throws Exception {
        log.info("==current profiles==: {}", profies);
    }
}
