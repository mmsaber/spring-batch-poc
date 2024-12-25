package com.example.concurrentcsvprocessor.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Using JdbcTemplate instead of hibernate to reduce the abstraction layers as much as possible for faster persistence.
 * Using raw JDBC might give a better performance, but I didn't want to go that far.
 */
@Repository
@RequiredArgsConstructor
public class LogRepository {
    private static final String INSERT_FLAT_FILE_PARSE_EXCEPTION_LOG_SQL =
            "INSERT INTO flat_file_parse_exception_logs (error_message, stack_trace, job_id, line_number) VALUES (?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void log(FlatFileParseException ex, Long jobId) {
        Assert.notNull(ex, "ex cannot be null.");
        Assert.notNull(jobId, "jobId cannot be null.");

        String stackTrace = getStackTrace(ex);
        String errorMessage = ex.getMessage();
        int lineNumber = ex.getLineNumber();
        jdbcTemplate.update(INSERT_FLAT_FILE_PARSE_EXCEPTION_LOG_SQL, errorMessage, stackTrace, jobId, lineNumber);
    }


    private String getStackTrace(Throwable e) {
        StringWriter stackTraceStringWriter = new StringWriter();
        PrintWriter stackTracePrintWriter = new PrintWriter(stackTraceStringWriter);
        e.printStackTrace(stackTracePrintWriter);
        return stackTraceStringWriter.toString();
    }
}
