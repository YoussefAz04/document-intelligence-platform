package com.docintel.backend.document;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class HybridSearchController {

    private final HybridSearchService hybridSearchService;

    public HybridSearchController(HybridSearchService hybridSearchService) {
        this.hybridSearchService = hybridSearchService;
    }

    @GetMapping("/hybrid")
    public List<HybridSearchResult> search(
            @RequestParam String query,
            @RequestParam(required = false) Integer limit
    ) {
        return hybridSearchService.search(query, limit);
    }
}
