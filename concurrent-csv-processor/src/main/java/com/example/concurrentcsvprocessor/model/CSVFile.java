package com.example.concurrentcsvprocessor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "csv_files")
public class CSVFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String filePath;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;
    private Long numberOfEntries;
    private String fileId;

    public enum ProcessingStatus {
        UNDER_PROCESSING,
        PROCESSED,
        FAILED,
    }

}
