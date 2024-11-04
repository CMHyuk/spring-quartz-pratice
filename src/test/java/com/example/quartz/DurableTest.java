package com.example.quartz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DurableTest {

    @Autowired
    private Scheduler scheduler;

    @BeforeEach
    public void setUp() throws SchedulerException {
        scheduler.clear();
    }

    @Test
    @DisplayName("isDurable이 false면 Trigger가 삭제되면 JobDetail도 삭제된다.")
    void deleteTest() throws Exception {
        // given
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity("testJob", "group")
                .storeDurably(false)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testTrigger", "group")
                .forJob(jobDetail)
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);

        // when
        scheduler.unscheduleJob(trigger.getKey());

        // then
        assertThat(scheduler.getJobDetail(jobDetail.getKey())).isNull();
    }

    @Test
    @DisplayName("isDurable이 true면 Trigger가 삭제되어도 JobDetail은 삭제되지 않는다.")
    void doesNotDeleteTest() throws Exception {
        // given
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity("testJob", "group")
                .storeDurably(true)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("testTrigger", "group")
                .forJob(jobDetail)
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);

        // when
        scheduler.unscheduleJob(trigger.getKey());

        // then
        assertThat(scheduler.getJobDetail(jobDetail.getKey())).isNotNull();
    }

    private static class TestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
        }
    }

}
