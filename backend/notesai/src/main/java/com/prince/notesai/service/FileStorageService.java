package com.prince.notesai.service;

import com.prince.notesai.entity.Document;
import com.prince.notesai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";

    private final DocumentRepository documentRepository;
    private final PdfExtractionService pdfExtractionService;
    private final DocumentProcessingService documentProcessingService;

    public Document saveFile(MultipartFile file, String sessionId) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("[FileStorage] Created uploads directory at: {}", uploadPath.toAbsolutePath());
        }

        String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("[FileStorage] Saved file: {} ({} bytes) → {}",
                file.getOriginalFilename(), file.getSize(), filePath);

        Document document = Document.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadStatus("PROCESSING")
                .uploadedAt(LocalDateTime.now())
                .sessionId(sessionId)
                .build();

        Document savedDocument = documentRepository.save(document);
        log.info("[FileStorage] Document saved to DB: id={}", savedDocument.getId());

        Map<Integer, String> pageTexts =
                pdfExtractionService.extractTextByPage(filePath.toFile());

        if (pageTexts.isEmpty()) {
            log.warn("[FileStorage] No text extracted from '{}'. " +
                    "File may be image-only or corrupt.", file.getOriginalFilename());
            savedDocument.setUploadStatus("FAILED_NO_TEXT");
            documentRepository.save(savedDocument);
            return savedDocument;
        }

        documentProcessingService.process(savedDocument, pageTexts);

        savedDocument.setUploadStatus("READY");
        documentRepository.save(savedDocument);

        log.info("[FileStorage] ✅ Document fully processed: id={}, file='{}'",
                savedDocument.getId(), file.getOriginalFilename());

        return savedDocument;
    }
}