package com.example.concurrentcsvprocessor.model;

import com.example.concurrentcsvprocessor.model.dto.Operation;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public class ProcessedExportImportData {
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

    private Map<Operation, Number> operationsResult = Collections.emptyMap();

}
