package com.example.exportimportapi.multiply;

import com.example.exportimportapi.multiply.model.ExportImportMultiplyData;
import com.example.exportimportapi.multiply.model.ProcessedExportImportMultiplyDataResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ExportImportMultiplyRestController {

    @GetMapping("/multiply")
    public Mono<ProcessedExportImportMultiplyDataResponse> getMultiplication(Mono<ExportImportMultiplyData> itemMono) {
        return itemMono.map(item -> {
            long multiplication = Long.parseLong(item.getQ2()) * Long.parseLong(item.getValue());
            return new ProcessedExportImportMultiplyDataResponse(multiplication);
        });
    }

}
