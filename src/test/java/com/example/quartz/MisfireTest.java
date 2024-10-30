package com.example.quartz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MisfireTest {

    private static final String CRON_EXPRESSION = "*/5 * * * * ?";

    @Autowired
    private Scheduler scheduler;

    private static final AtomicBoolean wasExecuted = new AtomicBoolean(false);

    @BeforeEach
    public void setUp() throws SchedulerException {
        scheduler.clear();
        wasExecuted.set(false);
    }

    @Nested
    @DisplayName("미스파이어가 발생하면")
    class MisfirePolicyTest {

        @Test
        void 즉시_실행한다() throws Exception {
            // given
            createSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION)
                    .withMisfireHandlingInstructionFireAndProceed());

            // when
            triggerMisfire();

            // then
            assertThat(wasExecuted).isTrue();
        }

        @Test
        void 실행하지_않는다() throws Exception {
            // given
            createSchedule(CronScheduleBuilder.cronSchedule(CRON_EXPRESSION)
                    .withMisfireHandlingInstructionDoNothing());

            // when
            triggerMisfire();

            // then
            assertThat(wasExecuted).isFalse();
        }

        private void createSchedule(CronScheduleBuilder scheduleBuilder) throws SchedulerException {
            JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                    .withIdentity("fireAndProceedJob", "testGroup")
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("fireAndProceedTrigger", "testGroup")
                    .withSchedule(scheduleBuilder)
                    .startNow()
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        }

        private void triggerMisfire() throws SchedulerException, InterruptedException {
            scheduler.standby();
            Thread.sleep(7000);
            scheduler.start();
            Thread.sleep(500);
        }

    }

    public static class TestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            wasExecuted.set(true);
        }
    }

}
