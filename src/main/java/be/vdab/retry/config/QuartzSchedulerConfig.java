package be.vdab.retry.config;

import be.vdab.retry.jobs.RetryJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;

@Configuration
@Slf4j
public class QuartzSchedulerConfig {

    @Bean
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext applicationContext) {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean scheduler(Map<String, JobDetail> jobMap, Map<String, Trigger> triggers, DataSource quartzDataSource, ApplicationContext applicationContext) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.yml"));
        schedulerFactory.setJobFactory(springBeanJobFactory(applicationContext));

        JobDetail[] jobArray = jobMap.values().toArray(new JobDetail[0]);
        Trigger[] triggerArray = triggers.values().toArray(new Trigger[0]);

        schedulerFactory.setJobDetails(jobArray);
        schedulerFactory.setTriggers(triggerArray);
        schedulerFactory.setDataSource(quartzDataSource);
        return schedulerFactory;
    }

    @Bean
    public JobDetailFactoryBean jobDetailRetry() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(RetryJob.class);
        jobDetailFactory.setName("Qrtz_JobDetailRetry");
        jobDetailFactory.setDescription("Invoke Retry Job...");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }

    @Bean
    public SimpleTriggerFactoryBean triggerRetry(@Qualifier("jobDetailRetry") JobDetail job, ApplicationProperties properties) {
        SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
        trigger.setJobDetail(job);

        long frequencyInSec = properties.getQuartzRetriesInterval();
        int repeatCount = properties.getQuartzRetriesRepeatCount();
        log.info("Configuring {} to fire every {} seconds with repeatCount {}", job.getKey(), frequencyInSec, repeatCount);
        trigger.setRepeatInterval(frequencyInSec * 1000);
        trigger.setRepeatCount(repeatCount);
        trigger.setName("Qrtz_RetryTrigger");
        return trigger;
    }

    @Profile("local")
    @QuartzDataSource
    public DataSource quartzLocalDataSource(Environment env) {
        return DataSourceBuilder.create()
                .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
                .url(env.getProperty("spring.datasource.url"))
                .username(env.getProperty("spring.datasource.username"))
                .password(env.getProperty("spring.datasource.password"))
                .build();
    }

    @Profile("!local")
    @QuartzDataSource
    public DataSource quartzServerDataSource(Environment env) throws NamingException {
        JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
        jndiFactory.setJndiName(env.getProperty("spring.datasource.jndi-name"));
        jndiFactory.setResourceRef(true);
        jndiFactory.afterPropertiesSet();
        return (DataSource) jndiFactory.getObject();
    }

}
