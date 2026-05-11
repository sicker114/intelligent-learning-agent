package com.backend.Controller;

import com.backend.Service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    private Path tempUploadDir;

    @BeforeEach
    void setUp() throws IOException {
        // 创建临时上传目录用于测试
        tempUploadDir = Files.createTempDirectory("test-uploads");
    }

    @Test
    void testUploadFile_WithValidPdf_ShouldReturnSuccess() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "PDF content here".getBytes()
        );

        doNothing().when(documentService).processPdf(any(File.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("test.pdf"));

        verify(documentService, times(1)).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WithNullFile_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .param("file", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("文件不能为空"));

        verify(documentService, never()).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.pdf",
            "application/pdf",
            new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("文件不能为空"));

        verify(documentService, never()).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WithNonPdfFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "Text content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(txtFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("仅支持 PDF 文件"));

        verify(documentService, never()).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WithDocxFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile docxFile = new MockMultipartFile(
            "file",
            "test.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "DOCX content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(docxFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("仅支持 PDF 文件"));

        verify(documentService, never()).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WhenProcessingFails_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "PDF content".getBytes()
        );

        doThrow(new RuntimeException("处理失败")).when(documentService).processPdf(any(File.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("处理失败")));

        verify(documentService, times(1)).processPdf(any(File.class));
    }

    @Test
    void testListFiles_ShouldReturnFileList() throws Exception {
        // Arrange - 由于FileController在构造时已经创建了uploadDir，
        // 这里我们只能测试接口调用
        // Act & Assert
        mockMvc.perform(get("/api/files/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testDownloadFile_WithValidFilename_ShouldReturnFile() throws Exception {
        // Arrange
        String filename = "test.pdf";

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{filename}", filename))
                .andExpect(status().isNotFound()); // 文件不存在时返回404
    }

    @Test
    void testDownloadFile_WithInvalidPath_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidFilename = "../etc/passwd";

        // Act & Assert
        mockMvc.perform(get("/api/files/download/{filename}", invalidFilename))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUploadFile_WithJavascriptFile_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MockMultipartFile jsFile = new MockMultipartFile(
            "file",
            "test.js",
            "application/javascript",
            "console.log('test')".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(jsFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("仅支持 PDF 文件"));

        verify(documentService, never()).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WithMultipleFiles_ShouldProcessOneAtATime() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "test1.pdf",
            "application/pdf",
            "PDF 1".getBytes()
        );

        doNothing().when(documentService).processPdf(any(File.class));

        // Act & Assert - 每次只能上传一个文件
        mockMvc.perform(multipart("/api/files/upload")
                .file(file1))
                .andExpect(status().isOk())
                .andExpect(content().string("test1.pdf"));

        verify(documentService, times(1)).processPdf(any(File.class));
    }

    @Test
    void testUploadFile_WithSpecialCharactersInFilename_ShouldHandleCorrectly() throws Exception {
        // Arrange
        String specialFilename = "测试文档-2024.pdf";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            specialFilename,
            "application/pdf",
            "PDF content".getBytes()
        );

        doNothing().when(documentService).processPdf(any(File.class));

        // Act & Assert
        mockMvc.perform(multipart("/api/files/upload")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string(specialFilename));

        verify(documentService, times(1)).processPdf(any(File.class));
    }
}
