package com.example.concurrentcsvprocessor.config.batch;

import com.example.concurrentcsvprocessor.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.AlwaysRetryPolicy;
import org.springframework.web.client.ResourceAccessException;

import java.net.BindException;
import java.net.ConnectException;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SimpleRetryPolicy extends AlwaysRetryPolicy {
    private final SettingService settingService;
    private final AtomicInteger serverIsDownRetryCount;
    private final AtomicInteger connectionRefusedRetryCount;


    public SimpleRetryPolicy(SettingService settingService) {
        this.settingService = settingService;
        this.serverIsDownRetryCount = new AtomicInteger(0);
        this.connectionRefusedRetryCount = new AtomicInteger(0);
    }

    @Override
    public boolean canRetry(@NonNull RetryContext context) {
        Throwable th = context.getLastThrowable();

        if (th instanceof ResourceAccessException rae) {
            Throwable cause = rae.getCause();
            if (cause instanceof ConnectException) {
                // server is not up
                return serverIsDownRetryCount.incrementAndGet() > settingService.getMaxRetryCount();
            } else if (cause instanceof BindException) {
                // resource limitation - the api or the client cannot handle all these concurrent connections
                // retry 10 times

                return connectionRefusedRetryCount.getAndIncrement() > 10;
            }
        }

        return false;
    }
}
