package com.example.quartz.elasticsearch.dto;

import com.example.quartz.validator.ValidMap;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class SearchAfterRequestDto {

    private int size = 20;

    private Map<String, String> wildcardSearch = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private WildCardOperationType wildcardSearchOperation = WildCardOperationType.AND;

    @ValidMap(fieldName = "sort")
    private Map<String, String> sort;

    private Map<String, Object> filter = new HashMap<>();

    private List<Object> searchAfter = new ArrayList<>();

}
