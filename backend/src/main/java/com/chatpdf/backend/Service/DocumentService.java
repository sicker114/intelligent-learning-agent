package com.chatpdf.backend.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void processPdf(File file) throws IOException {
        String text;
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        }

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("PDF 内容为空或无法解析");
        }

        // Spring AI M4 标准写法：构造函数 + 元数据
        Document pdfDoc = new Document(text, Map.of("source", file.getName()));

        // 智能分块（默认 500 token/块，保留重叠防语义断裂）
        TokenTextSplitter splitter = new TokenTextSplitter();
        List<Document> chunks = splitter.split(pdfDoc);

        // 自动向量化并入库（VectorStore 内置 EmbeddingModel）
        vectorStore.add(chunks);
    }
}
