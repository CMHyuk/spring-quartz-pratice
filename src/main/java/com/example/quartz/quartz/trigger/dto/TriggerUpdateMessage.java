package com.example.quartz.quartz.trigger.dto;

public record TriggerUpdateMessage(String triggerName, String triggerGroup, String cronExpression) {
}
