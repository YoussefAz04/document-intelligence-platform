package com.docintel.backend.document;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class KeywordSearchController {

    private final KeywordSearchService keywordSearchService;

    public KeywordSearchController(KeywordSearchService keywordSearchService) {
        this.keywordSearchService = keywordSearchService;
    }

    @GetMapping("/keyword")
    public List<KeywordSearchResult> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer limit
    ) {
        return keywordSearchService.search(query, limit);
    }
}
