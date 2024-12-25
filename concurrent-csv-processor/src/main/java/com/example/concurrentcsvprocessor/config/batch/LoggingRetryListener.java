package com.example.concurrentcsvprocessor.config.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

@Slf4j
@RequiredArgsConstructor
public class LoggingRetryListener implements RetryListener {
    private final Long jobId;

    @Override
    public <T, E extends Throwable> void onError(RetryContext context,
                                                 RetryCallback<T, E> callback,
                                                 Throwable throwable) {
        log.warn("Retrying for " + context.getRetryCount() + " time in job: " + jobId, throwable);
    }

}
