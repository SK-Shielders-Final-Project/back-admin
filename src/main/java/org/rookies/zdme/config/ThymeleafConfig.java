package org.rookies.zdme.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Bean
    public TemplateEngine stringTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.clearDialects(); // 기존 다이얼렉트 설정 초기화
        templateEngine.addDialect(new StandardDialect()); // 순수 StandardDialect 만 추가
        templateEngine.setTemplateResolver(new StringTemplateResolver());
        return templateEngine;
    }
}
