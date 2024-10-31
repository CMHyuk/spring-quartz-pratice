package com.example.quartz.quartz.trigger.dto;

import java.util.List;

public record CronExpressionUpdateRequest(
        String frequency,
        Time time,
        List<String> daysOfWeek,
        List<String> daysOfMonth,
        String specificDate
) {

    public record Time(int hour, int minute) {}

    public int getHour() {
        return this.time.hour;
    }

    public int getMinute() {
        return this.time.minute;
    }

}
