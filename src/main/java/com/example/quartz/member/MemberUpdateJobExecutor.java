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
public class MemberUpdateJobExecutor implements Job {

    private final MemberRepository memberRepository;

    @Override
    @DistributedLock(key = "memberUpdateLock")
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Member update job started.");
        Member member = memberRepository.findByName("멤버")
                .orElseThrow(IllegalArgumentException::new);
        member.updateName("새로운 멤버");
        memberRepository.save(member);
        log.info("Member update job finished.");
    }

}
