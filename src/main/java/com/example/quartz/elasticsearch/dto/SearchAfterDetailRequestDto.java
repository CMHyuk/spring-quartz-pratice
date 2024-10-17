package com.example.quartz.elasticsearch.dto;

import com.example.quartz.validator.ValidMap;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAfterDetailRequestDto {

    private int size = 20;

    @ValidMap(fieldName = "sort")
    private Map<String, String> sort;

    @ValidMap(fieldName = "must")
    private Map<String, Object> must = new HashMap<>();

    private Map<String, Object> should = new HashMap<>();

    private Map<String, Object> mustNot = new HashMap<>();

    private List<Object> searchAfter = new ArrayList<>();
}
