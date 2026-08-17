package com.docintel.backend.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class TextExtractionServiceTests {

    private final TextExtractionService textExtractionService = new TextExtractionService();

    @Test
    void extractsPdfTextWithOneBasedPageNumbers() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "regulations.pdf",
                "application/pdf",
                createTwoPagePdf()
        );

        List<ExtractedPage> pages = textExtractionService.extract(file, "application/pdf");

        assertThat(pages).hasSize(2);
        assertThat(pages.get(0).pageNumber()).isEqualTo(1);
        assertThat(pages.get(0).content()).contains("Admission requirements");
        assertThat(pages.get(1).pageNumber()).isEqualTo(2);
        assertThat(pages.get(1).content()).contains("Scholarship rules");
    }

    @Test
    void leavesPageNumberEmptyForPlainText() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.txt",
                "text/plain",
                "Student handbook content".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        List<ExtractedPage> pages = textExtractionService.extract(file, "text/plain");

        assertThat(pages).containsExactly(new ExtractedPage(null, "Student handbook content"));
    }

    private byte[] createTwoPagePdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addPage(document, "Admission requirements");
            addPage(document, "Scholarship rules");
            document.save(output);
            return output.toByteArray();
        }
    }

    private void addPage(PDDocument document, String text) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(72, 720);
            contentStream.showText(text);
            contentStream.endText();
        }
    }
}
