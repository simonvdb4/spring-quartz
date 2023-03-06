package be.vdab.retry.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApplicationProperties {
    public static final String SPRING_APPLICATION_NAME = "${spring.application.name}";

    @Value("${spring.application.name}")
    private String springApplicationName;

    @Value("#{new Integer('${service.quartz.retries.repeat-count}')}")
    private int quartzRetriesRepeatCount;

    @Value("#{new Long('${service.quartz.retries.interval}')}")
    private long quartzRetriesInterval;
}
