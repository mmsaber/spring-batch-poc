package com.example.concurrentcsvprocessor.repository;

import com.example.concurrentcsvprocessor.model.CSVFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CSVFileRepository extends JpaRepository<CSVFile, Long> {

    boolean existsByFileId(String fileId);

    @Query("SELECT f FROM CSVFile f WHERE f.fileId = ?1")
    Optional<CSVFile> findByFileId(String fileId);
}
