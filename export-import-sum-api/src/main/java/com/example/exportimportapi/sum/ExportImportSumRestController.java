//package com.example.exportimportapi.sum;
//
//import com.example.exportimportapi.sum.model.ExportImportData;
//import com.example.exportimportapi.sum.model.ProcessedExportImportDataResponse;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
//@RestController
//public class ExportImportSumRestController {
//
//    @GetMapping("/sum")
//    public Mono<ProcessedExportImportDataResponse> getSum(Mono<ExportImportData> itemMono) {
//        return itemMono.map(item -> {
//            int sum = Integer.parseInt(item.getQ2()) + Integer.parseInt(item.getValue());
//            return new ProcessedExportImportDataResponse(sum);
//        });
//    }
//
//}
