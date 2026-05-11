package com.backend.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@DisplayName("DocumentService 单元测试")
class DocumentServiceTest {

    private VectorStore mockVectorStore;
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockVectorStore = mock(VectorStore.class);
        documentService = new DocumentService(mockVectorStore);
    }

    @Test
    @DisplayName("成功处理PDF文件")
    void testProcessPdf_Success() throws IOException {
        File pdfFile = createSimplePdf();

        assertDoesNotThrow(() -> documentService.processPdf(pdfFile));

        verify(mockVectorStore, times(1)).add(anyList());

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockVectorStore).add(captor.capture());

        List<Document> capturedDocs = captor.getValue();
        assertNotNull(capturedDocs);
        assertFalse(capturedDocs.isEmpty());

        Document firstDoc = capturedDocs.get(0);
        assertTrue(firstDoc.getMetadata().containsKey("source"));
        assertEquals(pdfFile.getName(), firstDoc.getMetadata().get("source"));
    }

    @Test
    @DisplayName("处理空PDF文件抛出异常")
    void testProcessPdf_EmptyContent_ThrowsException() throws IOException {
        File emptyPdf = createEmptyPdf();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> documentService.processPdf(emptyPdf)
        );

        assertTrue(exception.getMessage().contains("PDF 内容为空或无法解析"));
        verify(mockVectorStore, never()).add(anyList());
    }

    @Test
    @DisplayName("处理null文件抛出异常")
    void testProcessPdf_NullFile_ThrowsException() {
        assertThrows(
            IOException.class,
            () -> documentService.processPdf(null)
        );
    }

    @Test
    @DisplayName("验证分块逻辑")
    void testProcessPdf_VerifyChunking() throws IOException {
        File pdfFile = createSimplePdf();

        documentService.processPdf(pdfFile);

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockVectorStore).add(captor.capture());

        List<Document> chunks = captor.getValue();
        assertNotNull(chunks);

        chunks.forEach(chunk -> {
            assertNotNull(chunk.getText());
            assertFalse(chunk.getText().isBlank());
            assertNotNull(chunk.getMetadata());
        });
    }

    @Test
    @DisplayName("多次处理同一文件")
    void testProcessPdf_MultipleTimes() throws IOException {
        File pdfFile = createSimplePdf();

        assertDoesNotThrow(() -> documentService.processPdf(pdfFile));
        assertDoesNotThrow(() -> documentService.processPdf(pdfFile));

        verify(mockVectorStore, times(2)).add(anyList());
    }

    private File createSimplePdf() throws IOException {
        File pdfFile = tempDir.resolve("test.pdf").toFile();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            document.save(pdfFile);
        }

        return pdfFile;
    }

    private File createEmptyPdf() throws IOException {
        File pdfFile = tempDir.resolve("empty.pdf").toFile();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            document.save(pdfFile);
        }

        return pdfFile;
    }
}
