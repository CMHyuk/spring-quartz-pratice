package com.example.quartz.quartz.trigger.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CronExpressionGeneratorTest {

    @Test
    @DisplayName("매 시간마다 실행하는 크론 표현식 생성")
    void generateHourlyCronExpression() {
        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.HOURLY, null, 0, 15, null);

        // then
        assertThat(cron).isEqualTo("0 15 * * * ?");
    }

    @Test
    @DisplayName("매일 오전 9시에 실행하는 크론 표현식 생성")
    void generateDailyCronExpression() {
        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.DAILY, null, 9, 0, null);

        // then
        assertThat(cron).isEqualTo("0 0 9 * * ?");
    }

    @Test
    @DisplayName("매주 월, 수, 금 오후 2시에 실행하는 크론 표현식 생성")
    void generateWeeklyCronExpression() {
        // given
        List<String> days = List.of("MON", "WED", "FRI");

        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.WEEKLY, days, 14, 0, null);

        // then
        assertThat(cron).isEqualTo("0 0 14 ? * MON,WED,FRI *");
    }

    @Test
    @DisplayName("매월 1일, 15일 오전 10시에 실행하는 크론 표현식 생성")
    void generateMonthlyCronExpression() {
        // given
        List<String> daysOfMonth = List.of("1", "15");

        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.MONTHLY, daysOfMonth, 10, 0, null);

        // then
        assertThat(cron).isEqualTo("0 0 10 1,15 * ?");
    }

    @Test
    @DisplayName("특정 날짜에 실행하는 크론 표현식 생성")
    void generateSpecificDateCronExpression() {
        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.SPECIFIC_DATE, null, 15, 0, "2024-12-25");

        // then
        assertThat(cron).isEqualTo("0 0 15 25 12 ? 2024");
    }

    @Test
    @DisplayName("매년 특정 날짜에 실행하는 크론 표현식 생성")
    void generateYearlyCronExpression() {
        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.YEARLY, null, 10, 30, "12-25");

        // then
        assertThat(cron).isEqualTo("0 30 10 25 12 ? *");
    }

    @Test
    @DisplayName("마지막 평일에 실행하는 크론 표현식 생성")
    void generateLastWeekdayCronExpression() {
        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.LAST_WEEKDAY, null, 9, 0, null);

        // then
        assertThat(cron).isEqualTo("0 0 9 LW * ?");
    }

    @Test
    @DisplayName("매월 마지막 날에 실행하는 크론 표현식 생성")
    void generateLastDayOfMonthCronExpression() {
        // when
        String cron = CronExpressionGenerator.generateCronExpression(Frequency.LAST_DAY_OF_MONTH, null, 17, 0, null);

        // then
        assertThat(cron).isEqualTo("0 0 17 L * ?");
    }

}
