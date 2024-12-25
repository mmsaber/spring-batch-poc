package com.example.concurrentcsvprocessor.config;

import com.example.concurrentcsvprocessor.config.batch.*;
import com.example.concurrentcsvprocessor.constants.BatchConstants;
import com.example.concurrentcsvprocessor.model.ExportImportData;
import com.example.concurrentcsvprocessor.model.ProcessedExportImportData;
import com.example.concurrentcsvprocessor.model.dto.Operation;
import com.example.concurrentcsvprocessor.repository.LogRepository;
import com.example.concurrentcsvprocessor.service.FileSystemService;
import com.example.concurrentcsvprocessor.service.SettingService;
import com.example.concurrentcsvprocessor.util.ArrayUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.retry.RetryPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.concurrentcsvprocessor.constants.BatchConstants.*;

@Slf4j
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = true)
public class BatchConfig {
    private final JobRepository jobRepository;
    private final SettingService settingService;
    private final FileSystemService fileSystemService;
    private final PlatformTransactionManager transactionManager = new ResourcelessTransactionManager();


    @Bean
    public Job manipulateExportImportData(
            @Qualifier("step1Manager") Step step1Manager,
            @Qualifier("aggregatorStep") Step aggregatorStep,
            @Qualifier("cleanerStep") Step cleanerStep
    ) {

        return new JobBuilder("manipulateExportImportData", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1Manager)
                .next(aggregatorStep)
                .next(cleanerStep)
                .build();
    }

    @Bean
    public Step step1Manager(Partitioner partitioner, PartitionHandler partitionHandler) {

        return new StepBuilder(BatchConstants.FIRST_STEP_MANAGER_NAME, jobRepository)
                .partitioner(FIRST_STEP_SLAVE_STEP_NAME, partitioner)
                .partitionHandler(partitionHandler)
                .build();
    }


    @Bean
    @Lazy
    public Step step1Worker(FlatFileItemReader<ExportImportData> reader,
                            ItemProcessor<ExportImportData, ProcessedExportImportData> processor,
                            FlatFileItemWriter<ProcessedExportImportData> writer,
                            LoggingSkipListener skipListener,
                            LoggingRetryListener retryListener,
                            RetryPolicy retryPolicy) {
        return new StepBuilder(BatchConstants.FIRST_STEP_SLAVE_STEP_NAME, jobRepository)
                .<ExportImportData, ProcessedExportImportData>chunk(100_000, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .retryPolicy(retryPolicy)
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .listener(skipListener)
                .listener(retryListener)
                .build();
    }


    @Bean
    @StepScope
    public FlatFileItemReader<ExportImportData> reader(
            @NonNull @Value("#{jobParameters['" + BASE_WORKING_DIRECTORY_JOB_PARAM_KEY + "']}") String workDirectoryBasePath,
            @NonNull @Value("#{stepExecutionContext['" + SLAVE_STEP_INPUT_FILE_NAME_JOB_PARAM_KEY + "']}") String inputFileName
    ) {

        int linesToSkip = fileSystemService.isFirstFilePart(inputFileName) ? 1 : 0;
        log.debug("lines to skip: {} for {}", linesToSkip, inputFileName);
        return new FlatFileItemReaderBuilder<ExportImportData>()
                .name("exportImportReader")
                .resource(new FileSystemResource(Path.of(workDirectoryBasePath, inputFileName)))
                .linesToSkip(linesToSkip)
                .saveState(true)
                .delimited()
                .names(BatchConstants.EXPORT_IMPORT_DATA_FIELDS)
                .targetType(ExportImportData.class)
                .build()
                ;
    }

    @Bean
    public ExecutorService taskExecutor() {

        return Executors.newFixedThreadPool(20); // Example size
    }

    @Bean
    @StepScope
    public ItemProcessor<ExportImportData, ProcessedExportImportData> processor(
            @NonNull @Value("#{jobParameters['" + FIRST_STEP_OPERATIONS_JOB_PARAM + "']}") Operation[] operations) {

        // Inject the ExecutorService bean
        ExecutorService executorService = taskExecutor();

        return new ExportImportProcessor(executorService, operations);
    }




    @Bean
    @StepScope
    public FlatFileItemWriter<ProcessedExportImportData> writer(
            @NonNull @Value("#{jobParameters['" + BASE_WORKING_DIRECTORY_JOB_PARAM_KEY + "']}") String workDirectoryBasePath,
            @NonNull @Value("#{stepExecutionContext['" + SLAVE_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY + "']}") String outFile,
            @Value("#{jobParameters['" + FIRST_STEP_OPERATIONS_JOB_PARAM + "']}") Operation[] operations
    ) {
        return new FlatFileItemWriterBuilder<ProcessedExportImportData>()
                .name("processedPersonWriter")
                .delimited()
                .fieldExtractor(item -> {
                    Object[] fields = new Object[BASE_PROCESSED_EXPORT_IMPORT_DATA_HEADER.length + operations.length];
                    fields[0] = item.getExpImp();
                    fields[1] = item.getYear();
                    fields[2] = item.getMonth();
                    fields[3] = item.getYear();
                    fields[4] = item.getCountry();
                    fields[5] = item.getCustom();
                    fields[6] = item.getHs2();
                    fields[7] = item.getHs4();
                    fields[8] = item.getHs6();
                    fields[9] = item.getHs9();
                    fields[10] = item.getQ1();
                    fields[11] = item.getQ2();
                    fields[12] = item.getValue();
                    // 13 + i;

                    for (int i = 0; i < operations.length; i++) {
                        Operation operation = operations[i];
                        Number result = item.getOperationsResult().get(operation);
                        fields[13 + i] = result;
                    }

                    return fields;
                })
                .resource(new FileSystemResource(Path.of(workDirectoryBasePath, outFile)))
                .build();
    }


    @Bean
    @JobScope
    public Step aggregatorStep(
            @NonNull @Value("#{jobParameters['" + BASE_WORKING_DIRECTORY_JOB_PARAM_KEY + "']}") String workDirectoryBasePath,
            @NonNull @Value("#{jobParameters['" + FIRST_STEP_OPERATIONS_JOB_PARAM + "']}") Operation[] operations,
            @NonNull @Value("#{jobParameters['" + AGGREGATOR_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY + "']}") String outputResourcePath
    ) {

        List<FileSystemResource> outputResources = fileSystemService.resolveSplitOutputFilePaths(Path.of(workDirectoryBasePath))
                .stream()
                .map(FileSystemResource::new)
                .toList();

        FlatFileItemReader<String> reader = new FlatFileItemReaderBuilder<String>()
                .name("csvFileReader")
                .lineMapper(new PassThroughLineMapper())
                .build();

        MultiResourceItemReader<String> multiResourceItemReader = new MultiResourceItemReader<>();
        multiResourceItemReader.setResources(outputResources.toArray(new Resource[0]));
        multiResourceItemReader.setName("csvFilesReaderAggregator");
        multiResourceItemReader.setDelegate(reader);

        FlatFileItemWriter<String> flatFileItemWriter = new FlatFileItemWriterBuilder<String>()
                .name("csvFilesWriterAggregator")
                .resource(new FileSystemResource(Path.of(workDirectoryBasePath, outputResourcePath)))
                .lineAggregator(new PassThroughLineAggregator<>())
                .headerCallback(writer -> {
                    String[] headerFields = ArrayUtils.concatenate(BASE_PROCESSED_EXPORT_IMPORT_DATA_HEADER, Operation.to(operations));
                    String header = String.join(",", headerFields);
                    writer.append(header);
                })
                .build();

        return new StepBuilder(AGGREGATOR_STEP_NAME, jobRepository)
                .<String, String>chunk(200_000, transactionManager)
                .reader(multiResourceItemReader)
                .writer(flatFileItemWriter)
                .build();
    }


    @Bean
    @JobScope
    public Step cleanerStep(
            @NonNull FileSystemService fileSystemService,
            @NonNull @Value("#{jobParameters['" + BASE_WORKING_DIRECTORY_JOB_PARAM_KEY + "']}") String workDirectoryBasePath,
            @NonNull @Value("#{jobParameters['" + INPUT_FILE_NAME_JOB_PARAM_KEY + "']}") String inputFileName
    ) {
        Path basePath = Path.of(workDirectoryBasePath);
        Path originalInputFilePath = basePath.resolve(inputFileName).toAbsolutePath();
        TaskletStep taskletStep = new TaskletStep(CLEAN_STEP_NAME);
        taskletStep.setJobRepository(jobRepository);
        taskletStep.setTransactionManager(transactionManager);
        taskletStep.setTasklet(new CleanerTasklet(basePath, originalInputFilePath, fileSystemService));
        return taskletStep;
    }

    @Bean
    @StepScope
    public LoggingSkipListener skipListener(@NonNull LogRepository logRepository,
                                            @NonNull @Value("#{stepExecution.jobExecution.jobId}") Long jobId) {
        return new LoggingSkipListener(logRepository, jobId);
    }

    @Bean
    @StepScope
    public LoggingRetryListener loggingRetryListener(@NonNull @Value("#{stepExecution.jobExecution.jobId}") Long jobId) {
        return new LoggingRetryListener(jobId);
    }


    @Bean
    public RetryPolicy simpleRetryPolicy(@NonNull SettingService settingService) {
        return new SimpleRetryPolicy(settingService);
    }


    @Bean
    @JobScope
    public PartitionHandler partitionHandler(@Qualifier("step1Worker") Step worker,
                                             @Qualifier("slaveWorkersThreadPoolTaskExecutor") TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(settingService.getNumberOfThreads());
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor);
        taskExecutorPartitionHandler.setStep(worker);
        return taskExecutorPartitionHandler;
    }


    @Bean
    @JobScope
    public CustomMultiResourcePartitioner partitioner(
            @Value("#{jobParameters['" + INPUT_FILE_NAME_JOB_PARAM_KEY + "']}") String inputFileName,
            @Value("#{jobParameters['" + BASE_WORKING_DIRECTORY_JOB_PARAM_KEY + "']}") String workDirectoryBasePath
    ) {
        CustomMultiResourcePartitioner partitioner = new CustomMultiResourcePartitioner();
        Path inputFilePath = Path.of(workDirectoryBasePath).resolve(inputFileName);
        List<FileSystemResource> splitInputResources = fileSystemService.resolveSplitInputFilePaths(inputFilePath)
                .stream()
                .map(FileSystemResource::new)
                .toList();

        partitioner.setResources(splitInputResources);
        return partitioner;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(settingService.getNumberOfThreads());
        taskExecutor.setCorePoolSize(settingService.getNumberOfThreads());
        taskExecutor.setQueueCapacity(settingService.getNumberOfThreads());
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public TaskExecutor slaveWorkersThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("step-1-worker-");
        taskExecutor.setMaxPoolSize(settingService.getNumberOfThreads());
        taskExecutor.setCorePoolSize(settingService.getNumberOfThreads());
        return taskExecutor;
    }

}