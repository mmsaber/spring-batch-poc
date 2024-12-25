package com.example.concurrentcsvprocessor.service;

import com.example.concurrentcsvprocessor.model.CSVFile;
import com.example.concurrentcsvprocessor.model.dto.Operation;
import com.example.concurrentcsvprocessor.repository.CSVFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class CSVFileService {
    private final CSVFileRepository csvFileRepository;
    private final CSVBatchLauncher csvBatchLauncher;

    private final static int LEFT_LIMIT = 48; // numeral '0'
    private final static int RIGHT_LIMIT = 122; // letter 'z'
    private final static int FILE_ID_LENGTH = 16;

    @Transactional
    public Optional<CSVFile> findByFileId(String fileId) {
        Assert.notNull(fileId, "fileId cannot be null");
        return csvFileRepository.findByFileId(fileId);
    }

    @Transactional
    public CSVFile prepareCSVFile() {
        String fileId = generateFileId();
        CSVFile csvFile = new CSVFile();
        csvFile.setFileId(fileId);
        csvFile.setStatus(CSVFile.ProcessingStatus.UNDER_PROCESSING);
        return csvFileRepository.save(csvFile);
    }

    @Transactional
    public String processCSVFile(MultipartFile multipartFile, Operation[] operations) throws Exception {
        Assert.notNull(multipartFile, "multipartFile cannot be null");

        String originalFilename = multipartFile.getOriginalFilename();
        log.info("processing csv file: [{}]", originalFilename);

        CSVFile csvFile = prepareCSVFile();

        csvBatchLauncher.processCSVFile(multipartFile, operations, csvFile);
        return csvFile.getFileId();
    }

    @Transactional(readOnly = true)
    public String generateFileId() {
        Random random = new Random();
        boolean duplicatedFileId = true;

        String fileId = null;
        while (duplicatedFileId) {
            fileId = random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(FILE_ID_LENGTH)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            duplicatedFileId = csvFileRepository.existsByFileId(fileId);
        }

        return fileId;
    }
}
