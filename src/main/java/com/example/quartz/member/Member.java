package com.example.quartz.member;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

@Entity
@Getter
@Document(indexName = "member_" + "*", createIndex = false)
@Setting(settingPath = "lower_case_normalizer_setting.json")
@NoArgsConstructor
public class Member {

    @Id
    private String id;

    private String name;

    public Member(String name) {
        this.name = name;
    }

    public void updateName(String newName) {
        this.name = newName;
    }

}
