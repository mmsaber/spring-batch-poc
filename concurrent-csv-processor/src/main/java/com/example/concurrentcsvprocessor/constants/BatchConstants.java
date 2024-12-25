package com.example.concurrentcsvprocessor.constants;

public final class BatchConstants {


    private BatchConstants() {
    }

    public static final String FIRST_STEP_MANAGER_NAME = "master-step";
    public static final String FIRST_STEP_SLAVE_STEP_NAME = "slave-step";
    public static final String AGGREGATOR_STEP_NAME = "aggregator-step";
    public static final String CLEAN_STEP_NAME = "clean-step";

    public static final String INPUT_FILE_NAME_JOB_PARAM_KEY = "INPUT_RESOURCE_JOB_PARAM_KEY";

    public static final String BASE_WORKING_DIRECTORY_JOB_PARAM_KEY = "BASE_WORKING_DIRECTORY";

    public static final String SLAVE_STEP_INPUT_FILE_NAME_JOB_PARAM_KEY = "SLAVE_STEP_INPUT_FILE_NAME_JOB_PARAM_KEY";
    public static final String FIRST_STEP_OPERATIONS_JOB_PARAM = "FIRST_STEP_OPERATIONS_JOB_PARAM";
    public static final String FIRST_STEP_OPERATIONS_HUMAN_READABLE_JOB_PARAM = "FIRST_STEP_OPERATIONS_HUMAN_READABLE_JOB_PARAM";
    public static final String SLAVE_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY = "SLAVE_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY";

    public static final String AGGREGATOR_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY = "OUTPUT_FILE_NAME_JOB_PARAM_KEY";

    public static final String[] BASE_PROCESSED_EXPORT_IMPORT_DATA_HEADER = {
            "exp_imp", "Year", "month", "ym", "Country", "Custom", "hs2", "hs4", "hs6", "hs9", "Q1", "Q2", "Value"
    };


    public static final String[] EXPORT_IMPORT_DATA_FIELDS = {
            "expImp", "year", "month", "ym", "country", "custom", "hs2", "hs4", "hs6", "hs9", "q1", "q2", "value"
    };

}
