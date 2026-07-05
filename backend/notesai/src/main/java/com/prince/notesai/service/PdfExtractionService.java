package com.prince.notesai.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class PdfExtractionService {

    public Map<Integer, String> extractTextByPage(File pdfFile) throws IOException {

        Map<Integer, String> pageTexts = new LinkedHashMap<>();

        try (PDDocument document = Loader.loadPDF(pdfFile)) {

            int totalPages = document.getNumberOfPages();
            log.info("[PdfExtraction] Extracting {} page(s) from: {}",
                    totalPages, pdfFile.getName());

            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String text = stripper.getText(document);

                if (text != null && !text.isBlank()) {
                    pageTexts.put(page, text);
                } else {
                    log.debug("[PdfExtraction] Page {} appears empty or image-only — skipped.", page);
                }
            }

            log.info("[PdfExtraction] Extracted text from {}/{} pages.",
                    pageTexts.size(), totalPages);
        }

        return pageTexts;
    }

    public String extractText(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}