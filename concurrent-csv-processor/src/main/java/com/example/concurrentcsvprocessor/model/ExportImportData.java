package com.example.concurrentcsvprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportImportData {
    private String expImp;
    private String year;
    private String month;
    private String ym;
    private String country;
    private String custom;
    private String hs2;
    private String hs4;
    private String hs6;
    private String hs9;
    private String q1;
    private String q2;
    private String value;
}
