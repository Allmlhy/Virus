package com.dhr.maven.virus_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")   // 允许所有请求路径
                        .allowedOriginPatterns("*")  // 允许所有域名访问，Spring Boot 2.4+推荐用allowedOriginPatterns代替allowedOrigins支持通配符
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")  // 允许所有请求头
                        .allowCredentials(true) // 允许携带凭证cookie
                        .maxAge(3600); // 预检请求缓存时间（秒）
            }
        };
    }
}
