package com.example.quartz.global;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@MappedSuperclass
@EqualsAndHashCode
public class BaseEntity {

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Id
    private String id;

    @CreatedDate
    @Field(type = FieldType.Date,  format = {}, pattern = TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIME_FORMAT)
    private LocalDateTime insertTime;

    @LastModifiedDate
    @Field(type = FieldType.Date, format = {}, pattern = TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = TIME_FORMAT)
    private LocalDateTime updateTime;

}
