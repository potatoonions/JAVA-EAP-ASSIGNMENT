package com.crs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {

    @Value("${crs.email.simulation-mode:true}")
    private boolean simulationMode;

    @Value("${crs.email.sender:noreply@crs.university.edu}")
    private String senderEmail;

    @Value("${crs.email.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${crs.session.timeout-minutes:30}")
    private int sessionTimeoutMinutes;

    @Value("${crs.session.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${crs.notification.duplicate-suppression-minutes:60}")
    private int duplicateSuppressionMinutes;

    public boolean isSimulationMode() { return simulationMode; }
    public String  getSenderEmail() { return senderEmail; }
    public String  getBaseUrl() { return baseUrl; }
    public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public int getMaxFailedAttempts() { return maxFailedAttempts; }
    public int getDuplicateSuppressionMinutes() { return duplicateSuppressionMinutes; }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
        s.setPoolSize(2);
        s.setThreadNamePrefix("crs-scheduler-");
        s.initialize();
        return s;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
