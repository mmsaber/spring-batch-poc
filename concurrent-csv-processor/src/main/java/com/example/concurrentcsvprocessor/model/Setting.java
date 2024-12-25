package com.example.concurrentcsvprocessor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "settings")
public class Setting {
    @Id
    @Column(name = "setting_key")
    private String settingKey;

    @Column(name = "setting_value", nullable = false)
    private String settingValue;

}
