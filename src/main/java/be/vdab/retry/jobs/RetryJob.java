package be.vdab.retry.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RetryJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("RetryJob ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());
        //Retry job kan hier ge-autowired en aangeroepen worden
        log.info("Next RetryJob scheduled @ {}", context.getNextFireTime());
    }
}