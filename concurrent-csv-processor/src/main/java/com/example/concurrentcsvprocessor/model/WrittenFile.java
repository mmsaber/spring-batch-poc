package com.example.concurrentcsvprocessor.model;

import java.nio.file.Path;

public record WrittenFile(Path filePath, long numberOfLines) {
}
