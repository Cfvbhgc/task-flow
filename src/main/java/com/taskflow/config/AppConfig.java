package com.taskflow.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Application-wide Spring configuration.
 *
 * <p>Registers shared beans that do not belong to a specific feature
 * module, such as request logging filters and formatters.</p>
 *
 * @author TaskFlow Team
 */
@Configuration
@Slf4j
public class AppConfig {

    /**
     * Creates a request logging filter that logs incoming HTTP requests
     * for debugging and audit purposes. The filter is only active when
     * the logging level for {@code org.springframework.web.filter} is
     * set to DEBUG.
     *
     * @return a configured {@link CommonsRequestLoggingFilter}
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(false);
        log.info("Request logging filter initialized");
        return loggingFilter;
    }
}
