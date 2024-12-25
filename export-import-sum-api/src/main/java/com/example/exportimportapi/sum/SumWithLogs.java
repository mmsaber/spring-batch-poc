package com.example.exportimportapi.sum;

import com.example.exportimportapi.sum.model.ExportImportSumData;
import com.example.exportimportapi.sum.model.ProcessedExportImportSumDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Random;

@RestController
@Slf4j
public class SumWithLogs {

    @RestController
    public class ExportImportSumRestController {

        @GetMapping("/sum")
        public Mono<ProcessedExportImportSumDataResponse> getSum(Mono<ExportImportSumData> itemMono) {
//            randomSleep();
//            log.info("Thread woke up after random sleep");
            return itemMono.map(item -> {
                int sum = Integer.parseInt(item.getQ2()) + Integer.parseInt(item.getValue());
                return new ProcessedExportImportSumDataResponse(sum);
            });
        }

    }
    private static void randomSleep() {
        Random random = new Random();
        // Generate a random number between 20 and 40 (inclusive)
        int sleepTime = 20 + random.nextInt(21); // 21 is exclusive upper bound

        try {
            // Sleep for the randomly generated time
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted");
        }
    }

}
