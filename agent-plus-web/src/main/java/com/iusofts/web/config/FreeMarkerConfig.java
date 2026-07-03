package com.iusofts.web.config;

import freemarker.template.Configuration;
import org.springframework.context.annotation.Bean;

/**
 * @author Ivan
 */
@org.springframework.context.annotation.Configuration
public class FreeMarkerConfig {

    @Bean("freeMarkerCfg")
    public Configuration getFreeMarkerConfiguration() {
        Configuration freeMarkerCfg = new Configuration(Configuration.VERSION_2_3_23);
        freeMarkerCfg.setClassForTemplateLoading(this.getClass(), "/ftl");
        freeMarkerCfg.setNumberFormat("#");
        return freeMarkerCfg;
    }
}