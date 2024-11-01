package com.example.quartz.quartz.trigger.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Frequency {

    HOURLY,
    DAILY,
    WEEKLY,
    MONTHLY,
    LAST_DAY_OF_MONTH,
    YEARLY,
    FIRST_WEEKDAY,
    LAST_WEEKDAY,
    SPECIFIC_DATE

    ;

    public static Frequency from(String frequency) {
        return Arrays.stream(values())
                .filter(fq -> fq.name().equals(frequency.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid frequency: " + frequency));
    }

    public List<String> getSelectedDays(List<String> daysOfWeek, List<String> daysOfMonth) {
        return switch (this) {
            case WEEKLY -> daysOfWeek;
            case MONTHLY -> daysOfMonth;
            default -> Collections.emptyList();
        };
    }

}

