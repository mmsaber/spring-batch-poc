package com.example.concurrentcsvprocessor.config.batch;

import com.example.concurrentcsvprocessor.model.ExportImportData;
import com.example.concurrentcsvprocessor.model.ProcessedExportImportData;
import com.example.concurrentcsvprocessor.model.dto.Operation;
import com.example.concurrentcsvprocessor.model.dto.ProcessedExportImportDataResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ExportImportProcessor implements ItemProcessor<ExportImportData, ProcessedExportImportData> {
    public static final String SUM_API_BASE_URL = "http://localhost:8050/sum";
    public static final String MULTIPLICATION_API_BASE_URL = "http://localhost:8051/multiply";
    public static final String DIVISION_API_BASE_URL = "http://localhost:8052/divide";

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .readTimeout(Duration.ofMinutes(5))
            .build();
    private final ExecutorService executorService;
    private final Operation[] operations;
    private static final ObjectReader OBJECT_READER = new ObjectMapper().reader();

    private final AtomicInteger processedEntries;


    public ExportImportProcessor(ExecutorService executorService, Operation[] operations) {
        this.executorService = executorService;
        this.operations = operations;
        this.processedEntries = new AtomicInteger(0);
    }

    @Override
    public ProcessedExportImportData process(@NonNull final ExportImportData item) throws Exception {
        Map<Operation, Future<Number>> operationsFutureResult = new HashMap<>();

        for (Operation operation : operations) {
            CompletableFuture<Number> futureResult = null;
            switch (operation) {
                case SUM:
                    futureResult = CompletableFuture.supplyAsync(() -> getSum(item), executorService);
                    break;
                case MULTIPLY:
                    futureResult = CompletableFuture.supplyAsync(() -> getMultiplication(item), executorService);
                    break;
                case DIVIDE:
                    futureResult = CompletableFuture.supplyAsync(() -> getDivide(item), executorService);
                    break;
            }
            operationsFutureResult.put(operation, futureResult);
        }

        ProcessedExportImportData processedExportImportData = new ProcessedExportImportData();
        // Copy properties from item to processedExportImportData
        processedExportImportData.setExpImp(item.getExpImp());
        processedExportImportData.setYear(item.getYear());
        processedExportImportData.setMonth(item.getMonth());
        processedExportImportData.setYm(item.getYm());
        processedExportImportData.setCountry(item.getCountry());
        processedExportImportData.setCustom(item.getCustom());
        processedExportImportData.setHs2(item.getHs2());
        processedExportImportData.setHs4(item.getHs4());
        processedExportImportData.setHs6(item.getHs6());
        processedExportImportData.setHs9(item.getHs9());
        processedExportImportData.setQ1(item.getQ1());
        processedExportImportData.setQ2(item.getQ2());
        processedExportImportData.setValue(item.getValue());

        // Collecting and handling results from futures
        Map<Operation, Number> operationsResult = new HashMap<>();
        for (Map.Entry<Operation, Future<Number>> operationFutureEntry : operationsFutureResult.entrySet()) {
            try {
                Number result = operationFutureEntry.getValue().get(); // Blocking call
                operationsResult.put(operationFutureEntry.getKey(), result);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing operation: " + operationFutureEntry.getKey(), e);
                // Handle the error appropriately. For example, you might want to throw an exception or continue with a default value.
                // throw new RuntimeException("Error processing operation: " + operationFutureEntry.getKey(), e);
            }
        }

        processedExportImportData.setOperationsResult(operationsResult);

        // Logging
        if (log.isDebugEnabled()) {
            log.debug("Converting [{}] to [{}]", item, processedExportImportData);
        }

        // Updating the processed entries count
        int processedEntriesCount = processedEntries.incrementAndGet();
        if (processedEntriesCount % 50_000 == 0) {
            log.info("Processed: [{}] entries so far", processedEntriesCount);
        }

        return processedExportImportData;
    }
    private <T> T handleFutureException(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error processing future task", e);
            throw new RuntimeException("Error in processing", e);
        }
    }
    private Number getMultiplication(ExportImportData item) {
        String url = buildUrl(MULTIPLICATION_API_BASE_URL, item);
        return executeApiRequest(url);
    }

    private Number getDivide(ExportImportData item) {
        String url = buildUrl(DIVISION_API_BASE_URL, item);
        return executeApiRequest(url);
    }
    private String convertToJson(ExportImportData item) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            log.error("Error converting item to JSON", e);
            throw new RuntimeException("Error converting item to JSON", e);
        }
    }
//    private Number getSum(ExportImportData item) {
//        String jsonPayload = convertToJson(item);
//        return executePostRequest(SUM_API_BASE_URL, jsonPayload);
//    }
    private Number executePostRequest(String baseUrl, String jsonPayload) {
        RequestBody body = RequestBody.create(jsonPayload, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(body)
                .build();

        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Unexpected code " + response);
            }
            byte[] bytes = response.body().bytes();
            ProcessedExportImportDataResponse responseBody = OBJECT_READER.readValue(bytes, ProcessedExportImportDataResponse.class);
            return responseBody.getResult();
        } catch (IOException e) {
            log.error("Error executing POST request", e);
            throw new RuntimeException("Error executing POST request", e);
        }
    }

    private Number getSum(ExportImportData item) {
        String url = buildUrl(SUM_API_BASE_URL, item);
        return executeApiRequest(url);
    }

    private String buildUrl(String baseUrl, ExportImportData item) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("expImp", item.getExpImp())
                .queryParam("year", item.getYear())
                .queryParam("month", item.getMonth())
                .queryParam("ym", item.getYm())
                .queryParam("country", item.getCountry())
                .queryParam("custom", item.getCustom())
                .queryParam("h2", item.getHs2())
                .queryParam("h4", item.getHs4())
                .queryParam("h6", item.getHs6())
                .queryParam("h9", item.getHs9())
                .queryParam("q1", item.getQ1())
                .queryParam("q2", item.getQ2())
                .queryParam("value", item.getValue())
                .encode()
                .toUriString();
    }

    private Number executeApiRequest(String url) {
        Request request = new Request.Builder().url(url).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            byte[] bytes = response.body().bytes();
            ProcessedExportImportDataResponse responseBody = OBJECT_READER.readValue(bytes, ProcessedExportImportDataResponse.class);
            return responseBody.getResult();
        } catch (IOException e) {
            log.error("Error executing API request", e);
            throw new RuntimeException("Error executing API request", e);
        }
    }


}
