package com.jsondebug.helper.controller;

import com.jsondebug.helper.controller.request.CompareRequest;
import com.jsondebug.helper.controller.request.ExtractRequest;
import com.jsondebug.helper.controller.request.FormatRequest;
import com.jsondebug.helper.controller.request.SearchRequest;
import com.jsondebug.helper.controller.response.*;
import com.jsondebug.helper.service.JsonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/json")
@CrossOrigin(origins = "*") // Allow the standalone HTML file to call the API
public class JsonController {

    private final JsonService jsonService;

    public JsonController(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    // ─── FORMAT ──────────────────────────────────────────────────────────────

    @PostMapping("/format")
    public ResponseEntity<?> format(@RequestBody FormatRequest req) {
        try {
            if (req.getInput() == null || req.getInput().isBlank()) {
                return badRequest("Input JSON is required");
            }
            String output = jsonService.format(req.getInput(), req.isPretty());
            return ResponseEntity.ok(new FormatResponse(output));
        } catch (Exception e) {
            return badRequest("Invalid JSON: " + e.getMessage());
        }
    }

    // ─── COMPARE ─────────────────────────────────────────────────────────────

    @PostMapping("/compare")
    public ResponseEntity<?> compare(@RequestBody CompareRequest req) {
        try {
            if (req.getJson1() == null || req.getJson2() == null) {
                return badRequest("Both json1 and json2 are required");
            }
            if (req.getJson1().isBlank()|| req.getJson2().isBlank()) {
                return badRequest("json2 is required");
            }
            var diffs = jsonService.compare(req.getJson1(), req.getJson2());
            return ResponseEntity.ok(new CompareResponse(diffs));
        } catch (Exception e) {
            return badRequest("Compare failed: " + e.getMessage());
        }
    }

    // ─── SEARCH ──────────────────────────────────────────────────────────────

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest req) {
        try {
            if (req.getJson() == null) {
                return badRequest("JSON input is required");
            }
            if (req.getQuery() == null || req.getQuery().isBlank()) {
                return badRequest("Search query is required");
            }
            var matches = jsonService.search(req.getJson(), req.getQuery());
            return ResponseEntity.ok(new SearchResponse(matches));
        } catch (Exception e) {
            return badRequest("Search failed: " + e.getMessage());
        }
    }

    // ─── EXTRACT (JSONPath) ───────────────────────────────────────────────────

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@RequestBody ExtractRequest req) {
        try {
            if (req.getJson() == null) {
                return badRequest("JSON input is required");
            }
            if (req.getPath() == null || req.getPath().isBlank()) {
                return badRequest("JSONPath expression is required");
            }
            var result = jsonService.extract(req.getJson(), req.getPath());
            return ResponseEntity.ok(new ExtractResponse(result));
        } catch (Exception e) {
            return badRequest("Extract failed: " + e.getMessage());
        }
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Bad Request", message));
    }
}
