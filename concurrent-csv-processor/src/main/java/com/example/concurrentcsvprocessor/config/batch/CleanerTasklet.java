package com.example.concurrentcsvprocessor.config.batch;

import com.example.concurrentcsvprocessor.service.FileSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Clean the temp files created while splitting and merging steps.
 * Leaving only the original input and output files
 */
@RequiredArgsConstructor
public class CleanerTasklet implements Tasklet {
    private final Path workingDirecotryPath;
    private final Path originalInputFilePath;
    private final FileSystemService fileSystemService;

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution,
                                @NonNull ChunkContext chunkContext) throws Exception {
        List<Path> splitInputFilePaths = fileSystemService.resolveSplitInputFilePaths(originalInputFilePath);
        List<Path> splitOutputFilePaths = fileSystemService.resolveSplitOutputFilePaths(workingDirecotryPath);

        for (Path splitInputFilePath : splitInputFilePaths) {
            Files.deleteIfExists(splitInputFilePath);
        }

        for (Path splitOutputFilePath : splitOutputFilePaths) {
            Files.deleteIfExists(splitOutputFilePath);
        }

        return RepeatStatus.FINISHED;
    }
}
