package com.example.concurrentcsvprocessor.controller;

import com.example.concurrentcsvprocessor.model.CSVFile;
import com.example.concurrentcsvprocessor.model.dto.FileId;
import com.example.concurrentcsvprocessor.model.dto.Operation;
import com.example.concurrentcsvprocessor.service.CSVFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ExportImportRestController {
    private final CSVFileService csvFileService;

    @PostMapping("/upload")
    public ResponseEntity<FileId> processCSVFile(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("operation") String[] operations) throws Exception {
        String csvFileId = csvFileService.processCSVFile(file, Operation.from(operations));
        return ResponseEntity.ok(new FileId(csvFileId));
    }

    @GetMapping("{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("fileId") String fileId) throws IOException {
        Optional<CSVFile> optionalCSVFile = csvFileService.findByFileId(fileId);
        if (optionalCSVFile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CSVFile csvFile = optionalCSVFile.get();
        if (csvFile.getStatus() == CSVFile.ProcessingStatus.PROCESSED) {
            FileSystemResource fileSystemResource = new FileSystemResource(csvFile.getFilePath());

            return ResponseEntity.status(HttpStatus.OK)
                    .contentLength(fileSystemResource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-disposition", "attachment; filename=" + fileId + ".csv")
                    .body(fileSystemResource);
        } else if (csvFile.getStatus() == CSVFile.ProcessingStatus.FAILED) {
            // FIXME how this should be handled?
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else if (csvFile.getStatus() == CSVFile.ProcessingStatus.UNDER_PROCESSING) {
            return ResponseEntity.status(HttpStatus.PROCESSING).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
