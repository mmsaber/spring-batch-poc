package com.example.concurrentcsvprocessor.bootstrap;

import com.example.concurrentcsvprocessor.model.Setting;
import com.example.concurrentcsvprocessor.repository.SettingRepository;
import com.example.concurrentcsvprocessor.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;


@RequiredArgsConstructor
@Slf4j
@Component
public class SettingBootstrap implements CommandLineRunner {
    private final SettingRepository settingRepository;
    private final TransactionTemplate txxt;

    @Override
    public void run(String... args) throws Exception {
        if (settingRepository.count() != 0) {
            return;
        }
        log.info("Initializing settings in the database");
        String numberOfThreadsSettingKey = SettingService.NUMBER_OF_THREADS_SETTING_KEY;
        String numberOfThreadsSettingValue = String.valueOf(Runtime.getRuntime().availableProcessors());

        String maxRetryCountSettingKey = SettingService.MAX_RETRY_COUNT_SETTING_KEY;
        String maxRetryCountSettingValue = String.valueOf(5);

        Setting numberOfThreadsSetting = new Setting(numberOfThreadsSettingKey, numberOfThreadsSettingValue);
        Setting maxRetryCountSetting = new Setting(maxRetryCountSettingKey, maxRetryCountSettingValue);
        txxt.executeWithoutResult(transactionStatus -> {
            settingRepository.saveAll(List.of(numberOfThreadsSetting, maxRetryCountSetting));
        });
    }
}
