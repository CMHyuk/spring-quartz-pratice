package com.example.quartz.quartz.trigger.model;

import org.quartz.CronScheduleBuilder;

import java.util.Arrays;

public enum MisfirePolicy {

    FIRE_AND_PROCEED {
        @Override
        public CronScheduleBuilder applyMisfirePolicy(CronScheduleBuilder builder) {
            return builder.withMisfireHandlingInstructionFireAndProceed();
        }
    },

    IGNORE_MISFIRES {
        @Override
        public CronScheduleBuilder applyMisfirePolicy(CronScheduleBuilder builder) {
            return builder.withMisfireHandlingInstructionIgnoreMisfires();
        }
    },

    DO_NOTHING {
        @Override
        public CronScheduleBuilder applyMisfirePolicy(CronScheduleBuilder builder) {
            return builder.withMisfireHandlingInstructionDoNothing();
        }
    };

    public static MisfirePolicy from(String misfirePolicy) {
        return Arrays.stream(values())
                .filter(policy -> policy.name().equals(misfirePolicy.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid misfirePolicy: " + misfirePolicy));
    }

    public abstract CronScheduleBuilder applyMisfirePolicy(CronScheduleBuilder builder);
}
