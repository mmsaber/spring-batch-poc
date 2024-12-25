package com.example.concurrentcsvprocessor.service;

import com.example.concurrentcsvprocessor.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingService {
    public final static String NUMBER_OF_THREADS_SETTING_KEY = "setting.batch.numberOfThreads";
    public static final int DEFAULT_NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    public static final String MAX_RETRY_COUNT_SETTING_KEY = "setting.batch.maxRetryCountSettingKey";
    public static final int DEFAULT_MAX_RETRY_COUNT_SETTING = 3;


    private final SettingRepository settingRepository;


    @Transactional(readOnly = true)
    public int getNumberOfThreads() {
        return settingRepository.findById(NUMBER_OF_THREADS_SETTING_KEY)
                .map(setting -> Integer.parseInt(setting.getSettingValue()))
                .orElseGet(() -> {
                    log.warn("Failed to find {} in the database, defaulting to {}", NUMBER_OF_THREADS_SETTING_KEY, DEFAULT_NUMBER_OF_THREADS);
                    return DEFAULT_NUMBER_OF_THREADS;
                });
    }


    @Transactional(readOnly = true)
    public int getMaxRetryCount() {
        return settingRepository.findById(MAX_RETRY_COUNT_SETTING_KEY)
                .map(setting -> Integer.parseInt(setting.getSettingValue()))
                .orElseGet(() -> {
                    log.warn("Failed to find {} in the database, defaulting to {}", MAX_RETRY_COUNT_SETTING_KEY, DEFAULT_MAX_RETRY_COUNT_SETTING);
                    return DEFAULT_MAX_RETRY_COUNT_SETTING;
                });
    }

}