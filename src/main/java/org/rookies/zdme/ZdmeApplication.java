package org.rookies.zdme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.rookies.zdme.config.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class ZdmeApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ZdmeApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ZdmeApplication.class, args);
    }
}
