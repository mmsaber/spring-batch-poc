package com.example.concurrentcsvprocessor.service;

import com.example.concurrentcsvprocessor.model.WrittenFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.observability.BatchMetrics;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemService {
    private final SettingService settingService;
    public static final String BASE_WORKING_DIRECTORY = "."; // the current src path

    /**
     * Uploads a multipart file to the file system
     *
     * @param fileToUpload the file to upload
     * @return the uploaded file path
     * @throws IOException              if an error happens while uploading the file
     * @throws IllegalArgumentException when {@code fileToUpload} is null.
     */
    public WrittenFile uploadFile(MultipartFile fileToUpload) throws IOException {
        Assert.notNull(fileToUpload, "fileToUpload cannot be null");

        String originalFileName = fileToUpload.getOriginalFilename() == null ? "input" : fileToUpload.getOriginalFilename();

        long millis = System.currentTimeMillis();

        String generatedWorkingDirectoryName = getStrippedFileName(originalFileName) + "_" + millis;
        String generatedFileName = generatedWorkingDirectoryName + ".csv";

        Path basePath = Path.of(BASE_WORKING_DIRECTORY, generatedWorkingDirectoryName);

        Files.createDirectories(basePath);


        Path uploadFilePath = basePath.resolve(generatedFileName).normalize().toAbsolutePath();

        log.info("uploading: {} to {}", generatedFileName, uploadFilePath);

        LocalDateTime start = LocalDateTime.now();
        long numberOfLines = 0;
        try (var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileToUpload.getBytes())));
             var writer = Files.newBufferedWriter(uploadFilePath)) {
            String line = reader.readLine();
            while (StringUtils.hasText(line)) {
                writer.append(line).append(System.lineSeparator());
                line = reader.readLine();
                numberOfLines++;
            }

            LocalDateTime end = LocalDateTime.now();

            Duration fileUploadDuration = BatchMetrics.calculateDuration(start, end);

            log.info("uploading: [{}] executed in [{}]", uploadFilePath, BatchMetrics.formatDuration(fileUploadDuration));
            return new WrittenFile(uploadFilePath, numberOfLines);
        }
    }


    /**
     * Splits a file path into N sub-files where N is {@link SettingService#getNumberOfThreads()}
     *
     * @param filePath      the original file path
     * @param numberOfLines the number of lines in the file
     * @return a list of split files
     * @throws IOException if an I/O error occurred while reading/writing the files.
     */
    public List<WrittenFile> splitFile(Path filePath,
                                       long numberOfLines) throws IOException {
        int numberOfParts = settingService.getNumberOfThreads();

        log.info("Partitioning '{}' to '{}' parts", filePath.getFileName(), numberOfParts);
        LocalDateTime start = LocalDateTime.now();

        long partSize = numberOfLines / numberOfParts;

        final List<WrittenFile> writtenFiles = new ArrayList<>();

        try (var reader = Files.newBufferedReader(filePath)) {

            for (int part = 0; part < numberOfParts; part++) {
                Path subPath = resolveSplitInputFilePath(filePath, part);
                long readLines = 0;
                long linesToRead = partSize;
                boolean lastPart = part == numberOfParts - 1;
                if (lastPart) {
                    linesToRead = partSize + numberOfLines % partSize;
                }

                try (var writer = Files.newBufferedWriter(subPath, StandardOpenOption.CREATE)) {
                    String line;

                    while (readLines < linesToRead && (line = reader.readLine()) != null) {
                        writer.append(line).append(System.lineSeparator());
                        readLines++;
                    }

                    writtenFiles.add(new WrittenFile(subPath, readLines));
                }
            }
        }
        LocalDateTime end = LocalDateTime.now();

        Duration fileUploadDuration = BatchMetrics.calculateDuration(start, end);

        log.info("splitting: '{}' executed in '{}'", filePath.getFileName(), BatchMetrics.formatDuration(fileUploadDuration));

        return writtenFiles;
    }

    /**
     * Removes the .csv suffix from the file name if exists
     *
     * @param fileName the file name
     * @return the stripped file name
     */
    public String getStrippedFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "input";
        }

        if (fileName.endsWith(".csv")) {
            if (fileName.length() == 4) {
                return "input";
            }

            return fileName.substring(0, fileName.length() - 4);
        }

        return fileName;
    }

    /**
     * Resolves the nth split file path from the original file path
     * <p>
     * If the original file path is: input.csv and the part number is 5,
     * the output of this method will be: input_5.csv
     * </p>
     *
     * @param originalFile the original file path
     * @param partNumber   the part number
     * @return the resolved path
     * @implNote the resolved path might not exist in the file system
     */
    public Path resolveSplitInputFilePath(Path originalFile, int partNumber) {
        String fileName = originalFile.getFileName().toString().split("\\.")[0] + "_" + partNumber + ".csv";
        return originalFile.getParent().resolve(fileName).normalize().toAbsolutePath();
    }

    /**
     * Gets the split output file name
     *
     * @param partNumber the part number
     * @return split output file name
     */
    public static String getSplitOutputFileName(int partNumber) {
        return "output_" + partNumber + ".csv";
    }

    /**
     * Resolve the nth split input file paths, where n is {@link SettingService#getNumberOfThreads()}
     *
     * @param originalInputFilePath the original input file
     * @return a list of split input file paths
     */
    public List<Path> resolveSplitInputFilePaths(Path originalInputFilePath) {
        List<Path> splitFilePaths = new ArrayList<>();
        for (int i = 0; i < settingService.getNumberOfThreads(); i++) {
            splitFilePaths.add(resolveSplitInputFilePath(originalInputFilePath, i));
        }
        return splitFilePaths;
    }

    /**
     * Resolve the nth split output file paths, where n is {@link SettingService#getNumberOfThreads()}
     *
     * @param originalOutputFilePath the original output file
     * @return a list of split output file paths
     */
    public List<Path> resolveSplitOutputFilePaths(Path originalOutputFilePath) {
        List<Path> splitFilePaths = new ArrayList<>();
        for (int i = 0; i < settingService.getNumberOfThreads(); i++) {
            splitFilePaths.add(originalOutputFilePath.resolve(getSplitOutputFileName(i)));
        }
        return splitFilePaths;
    }

    /**
     * Checks whether the given file name is the first part of the split files.
     *
     * @param splitInputFileName the split input file name
     * @return true if it's the first part, false otherwise
     */
    public boolean isFirstFilePart(String splitInputFileName) {
        Assert.hasText(splitInputFileName, "splitInputFileName cannot be empty");
        return splitInputFileName.endsWith("_0.csv");
    }
}
