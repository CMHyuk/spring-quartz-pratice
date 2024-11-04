package com.example.quartz.quartz.trigger.util;

import java.util.List;
import java.util.stream.Collectors;

public class CronExpressionGenerator {

    private static final List<String> DAYS_OF_WEEK = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    public static String generateCronExpression(Frequency frequency, List<String> selectedDays, int hour, int minute, String specificDate) {
        return switch (frequency) {
            case HOURLY -> String.format("0 %d * * * ?", minute);
            case DAILY -> String.format("0 %d %d * * ?", minute, hour);
            case WEEKLY -> String.format("0 %d %d ? * %s *", minute, hour, convertDaysToCronFormat(selectedDays));
            case MONTHLY -> String.format("0 %d %d %s * ?", minute, hour, String.join(",", selectedDays));
            case LAST_DAY_OF_MONTH -> String.format("0 %d %d L * ?", minute, hour);
            case YEARLY -> createYearlyCron(hour, minute, specificDate);
            case FIRST_WEEKDAY -> String.format("0 %d %d 1W * ?", minute, hour);
            case LAST_WEEKDAY -> String.format("0 %d %d LW * ?", minute, hour);
            case SPECIFIC_DATE -> createSpecificDateCron(hour, minute, specificDate);
        };
    }

    private static String convertDaysToCronFormat(List<String> days) {
        return days.stream()
                .filter(DAYS_OF_WEEK::contains)
                .collect(Collectors.joining(","));
    }

    private static String createSpecificDateCron(int hour, int minute, String specificDate) {
        String[] dateParts = specificDate.split("-");
        return String.format("0 %d %d %s %s ? %s", minute, hour, dateParts[2], dateParts[1], dateParts[0]);
    }

    private static String createYearlyCron(int hour, int minute, String specificDate) {
        String[] dateParts = specificDate.split("-");
        return String.format("0 %d %d %s %s ? *", minute, hour, dateParts[1], dateParts[0]);
    }

}
