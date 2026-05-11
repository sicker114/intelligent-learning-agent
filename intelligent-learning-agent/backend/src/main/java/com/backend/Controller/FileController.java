package com.backend.Controller;

import com.backend.Service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final DocumentService documentService;
    private final Path uploadDir;

    public FileController(DocumentService documentService) throws IOException {
        this.documentService = documentService;
        // 使用项目运行根目录下的 uploads 文件夹（Jar 同级目录）
        this.uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
        if (!Files.exists(this.uploadDir)) {
            Files.createDirectories(this.uploadDir);
            System.out.println("自动创建文件存储目录: " + uploadDir);
        }
    }

    /** 1. 上传 PDF 并解析入库 */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("仅支持 PDF 文件");
        }

        try {
            // 安全路径解析，防止路径穿越攻击
            Path dest = this.uploadDir.resolve(originalFilename).toAbsolutePath().normalize();
            file.transferTo(dest.toFile());
            System.out.println("原始文件已保存: " + dest);

            // 调用服务：提取文本 → 分块 → 向量化 → 存入 Qdrant
            documentService.processPdf(dest.toFile());
            return ResponseEntity.ok(originalFilename);//封装http响应
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("处理失败: " + e.getMessage());
        }
    }

    /** 2. 获取已上传文件列表 */
    @GetMapping("/list")
    public List<String> listFiles() {
        try {
            return Files.list(this.uploadDir)
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** 3. 下载原始 PDF */
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = this.uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

