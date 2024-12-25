package com.example.concurrentcsvprocessor.service;

import com.example.concurrentcsvprocessor.model.CSVFile;
import com.example.concurrentcsvprocessor.model.WrittenFile;
import com.example.concurrentcsvprocessor.model.dto.Operation;
import com.example.concurrentcsvprocessor.repository.CSVFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.example.concurrentcsvprocessor.constants.BatchConstants.*;

@Slf4j
@Component
public class CSVBatchLauncher {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final CSVFileRepository csvFileRepository;
    private final FileSystemService fileSystemService;
    private final TransactionTemplate txxt;

    private final TaskExecutor taskExecutor;

    public CSVBatchLauncher(JobLauncher jobLauncher,
                            Job job,
                            CSVFileRepository csvFileRepository,
                            FileSystemService fileSystemService,
                            TransactionTemplate txxt,
                            @Qualifier("threadPoolTaskExecutor") TaskExecutor taskExecutor) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.csvFileRepository = csvFileRepository;
        this.fileSystemService = fileSystemService;
        this.txxt = txxt;
        this.taskExecutor = taskExecutor;
    }

    public void processCSVFile(MultipartFile multipartFile, Operation[] operations, CSVFile underProcessCSVFile) throws Exception {
        Assert.notNull(multipartFile, "multipartFile cannot be null");

        String originalFilename = multipartFile.getOriginalFilename();
        log.info("processing csv file: [{}]", originalFilename);



        Operation[] distinctOperations = new LinkedHashSet<>(List.of(operations)).toArray(new Operation[0]);

        // THIS has to be blocking otherwise tomcat will remove the created directory for that file
        WrittenFile uploadedFilePath = fileSystemService.uploadFile(multipartFile);
        Path basePath = uploadedFilePath.filePath().toAbsolutePath().getParent();

        String outputFileName = fileSystemService.getStrippedFileName(basePath.getFileName().toString()) + "_output.csv";


        CompletableFuture.supplyAsync(() -> {
            try {
                Path processedFilePath = Path.of(basePath.toAbsolutePath().toString(), outputFileName);

                fileSystemService.splitFile(uploadedFilePath.filePath(), uploadedFilePath.numberOfLines());
                underProcessCSVFile.setFilePath(processedFilePath.toAbsolutePath().toString());
                underProcessCSVFile.setNumberOfEntries(uploadedFilePath.numberOfLines());

                CSVFile savedCSVFile = txxt.execute(status -> csvFileRepository.save(underProcessCSVFile));

                JobParameter<Operation[]> operationsJobParam = new JobParameter<>(distinctOperations, Operation[].class);

                JobParameters jobParameters = new JobParametersBuilder()
                        .addString(INPUT_FILE_NAME_JOB_PARAM_KEY, uploadedFilePath.filePath().getFileName().toString())
                        .addString(BASE_WORKING_DIRECTORY_JOB_PARAM_KEY, basePath.toString())
                        .addString(AGGREGATOR_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY, outputFileName)
                        .addJobParameter(FIRST_STEP_OPERATIONS_JOB_PARAM, operationsJobParam)
                        .addString(FIRST_STEP_OPERATIONS_HUMAN_READABLE_JOB_PARAM, Arrays.toString(Operation.to(distinctOperations)))
                        .addDate("startedAt", new Date())
                        .toJobParameters();
                JobExecution run = jobLauncher.run(job, jobParameters);

                txxt.executeWithoutResult(status -> {
                    if (run.getStatus() == BatchStatus.COMPLETED) {
                        savedCSVFile.setStatus(CSVFile.ProcessingStatus.PROCESSED);
                    } else {
                        savedCSVFile.setStatus(CSVFile.ProcessingStatus.FAILED);
                    }
                    csvFileRepository.save(savedCSVFile);
                });
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, taskExecutor);
    }


}
