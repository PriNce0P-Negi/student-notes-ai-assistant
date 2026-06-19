package com.prince.notesai.service;

import com.prince.notesai.entity.Document;
import com.prince.notesai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final DocumentRepository documentRepository;
    private final PdfExtractionService pdfExtractionService;
    private final DocumentProcessingService documentProcessingService;

    private static final String UPLOAD_DIR = "uploads";

    public Document saveFile(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFileName =
                UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        String extractedText =
                pdfExtractionService.extractText(filePath.toFile());

        Document document = Document.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadStatus("UPLOADED")
                .uploadedAt(LocalDateTime.now())
                .build();

        Document savedDocument =
                documentRepository.save(document);

        documentProcessingService.process(
                savedDocument,
                extractedText
        );

        return savedDocument;
    }
}