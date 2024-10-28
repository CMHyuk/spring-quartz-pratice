package com.example.quartz.member;

import com.example.quartz.redis.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSaveJobExecutor implements Job {

    private final MemberRepository memberRepository;

    @Override
    @DistributedLock(key = "memberJobLock")
    public void execute(JobExecutionContext jobExecutionContext) {

        log.info("Member save job started.");
        memberRepository.save();
        log.info("Member save job finished.");
    }

}
