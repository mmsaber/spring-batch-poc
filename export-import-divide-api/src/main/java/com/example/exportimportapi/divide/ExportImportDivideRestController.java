package com.example.exportimportapi.divide;

import com.example.exportimportapi.divide.model.ExportImportDivideData;
import com.example.exportimportapi.divide.model.ProcessedExportImportDivideDataResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ExportImportDivideRestController {

    @GetMapping("/divide")
    public Mono<ProcessedExportImportDivideDataResponse> getDivision(Mono<ExportImportDivideData> itemMono) {
        return itemMono.map(item -> {
            double division = Double.parseDouble(item.getQ2()) / Double.parseDouble(item.getValue());
            return new ProcessedExportImportDivideDataResponse(division);
        });
    }

}
