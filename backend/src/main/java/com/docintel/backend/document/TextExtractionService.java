package com.docintel.backend.document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

@Service
public class TextExtractionService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final AutoDetectParser parser = new AutoDetectParser();

    public List<ExtractedPage> extract(MultipartFile file, String contentType) {
        if (PDF_CONTENT_TYPE.equals(contentType)) {
            return extractPdfPages(file);
        }

        return List.of(new ExtractedPage(null, extractText(file)));
    }

    private List<ExtractedPage> extractPdfPages(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            List<ExtractedPage> pages = new ArrayList<>();

            for (int pageNumber = 1; pageNumber <= document.getNumberOfPages(); pageNumber++) {
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String content = normalize(stripper.getText(document));
                if (!content.isBlank()) {
                    pages.add(new ExtractedPage(pageNumber, content));
                }
            }

            return pages;
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not extract text from uploaded PDF.", exception);
        }
    }

    private String extractText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();

            parser.parse(inputStream, handler, metadata, parseContext);
            return normalize(handler.toString());
        } catch (IOException | SAXException | TikaException exception) {
            throw new IllegalArgumentException("Could not extract text from uploaded document.", exception);
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
