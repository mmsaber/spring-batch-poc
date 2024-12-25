package com.example.concurrentcsvprocessor.config.batch;

import com.example.concurrentcsvprocessor.model.ExportImportData;
import com.example.concurrentcsvprocessor.model.ProcessedExportImportData;
import com.example.concurrentcsvprocessor.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.lang.NonNull;

@Slf4j
@RequiredArgsConstructor
public class LoggingSkipListener implements SkipListener<ExportImportData, ProcessedExportImportData> {
    private final LogRepository logRepository;
    private final Long jobId;


    @Override
    public void onSkipInRead(@NonNull final Throwable t) {
        log.warn("Error while reading the file in job: " + jobId, t);
        if (t instanceof FlatFileParseException ex) {
            logRepository.log(ex, jobId);
        }
    }

}
