package com.example.quartz.quartz.trigger.util;

import java.util.List;
import java.util.stream.Collectors;

public class CronExpressionGenerator {

    private static final List<String> DAYS_OF_WEEK = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    public static String generateCronExpression(Frequency frequency, List<String> selectedDays, int hour, int minute, String specificDate) {
        return switch (frequency) {
            case HOURLY -> createHourlyCron(minute);
            case DAILY -> createDailyCron(hour, minute);
            case WEEKLY -> createWeeklyCron(selectedDays, hour, minute);
            case MONTHLY -> createMonthlyCron(selectedDays, hour, minute);
            case YEARLY -> createYearlyCron(specificDate, hour, minute);
            case LAST_WEEKDAY -> createLastWeekdayCron(hour, minute);
            case SPECIFIC_DATE -> createSpecificDateCron(specificDate, hour, minute);
        };
    }

    private static String createHourlyCron(int minute) {
        return String.format("0 %d * * * ?", minute);
    }

    private static String createDailyCron(int hour, int minute) {
        return String.format("0 %d %d * * ?", minute, hour);
    }

    private static String createWeeklyCron(List<String> selectedDays, int hour, int minute) {
        return String.format("0 %d %d ? * %s *", minute, hour, convertDaysToCronFormat(selectedDays));
    }

    private static String createMonthlyCron(List<String> selectedDays, int hour, int minute) {
        return String.format("0 %d %d %s * ?", minute, hour, String.join(",", selectedDays));
    }

    private static String createYearlyCron(String date, int hour, int minute) {
        String[] dateParts = date.split("-");
        return String.format("0 %d %d %s %s ? *", minute, hour, dateParts[1], dateParts[0]);
    }

    private static String createLastWeekdayCron(int hour, int minute) {
        return String.format("0 %d %d LW * ?", minute, hour);
    }

    private static String createSpecificDateCron(String date, int hour, int minute) {
        String[] dateParts = date.split("-");
        return String.format("0 %d %d %s %s ? %s", minute, hour, dateParts[2], dateParts[1], dateParts[0]);
    }

    private static String convertDaysToCronFormat(List<String> days) {
        return days.stream()
                .filter(DAYS_OF_WEEK::contains)
                .collect(Collectors.joining(","));
    }

}
