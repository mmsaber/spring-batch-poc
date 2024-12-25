//package com.example.concurrentcsvprocessor;
//
//import com.example.concurrentcsvprocessor.controller.ExportImportRestController;
//import com.example.concurrentcsvprocessor.service.CSVFileService;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//@SpringBootTest
//public class ExportImportRestControllerTest {
//
//	@Mock
//	private CSVFileService csvFileService;
//
//	@InjectMocks
//	private ExportImportRestController controller;
//
//	private MockMvc mockMvc;
//
//	@Test
//	public void testUploadCSVFile() throws Exception {
//		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
//
//		MockMultipartFile file = new MockMultipartFile(
//				"file",
//				"test.csv",
//				MediaType.TEXT_PLAIN_VALUE,
//				"id,name,value\n1,Item1,100".getBytes()
//		);
//
//		when(csvFileService.processFile(any(), any())).thenReturn("File uploaded successfully");
//
//		mockMvc.perform(MockMvcRequestBuilders.multipart("/upload")
//						.file(file)
//						.param("operation", "multiply"))
//				.andExpect(status().isOk())
//				.andExpect(jsonPath("$.message").value("File uploaded successfully"));
//	}
//
//	@Test
//	public void testDownloadCSVFile() throws Exception {
//		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
//
//		when(csvFileService.downloadProcessedFile(any())).thenReturn("Processed CSV Content");
//
//		mockMvc.perform(MockMvcRequestBuilders.get("/ml0r2KAz9Ortqkje"))
//				.andExpect(status().isOk());
//	}
//}
//
//@SpringBootTest
//public class CSVFileServiceTest {
//
//	@Mock
//	private CSVFileService csvFileService;
//
//	@Test
//	public void testProcessFile() {
//		String result = csvFileService.processFile(null, "multiply");
//		assert result != null;
//	}
//
//	@Test
//	public void testDownloadProcessedFile() {
//		String result = csvFileService.downloadProcessedFile("ml0r2KAz9Ortqkje");
//		assert result.equals("Processed CSV Content");
//	}
//}
//
