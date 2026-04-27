package com.jsondebug.helper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jsondebug.helper.controller.response.CompareResponse.Difference;
import com.jsondebug.helper.controller.response.SearchResponse.Match;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JsonService {

    // Reuse ObjectMapper instances — they are thread-safe
    private final ObjectMapper prettyMapper;
    private final ObjectMapper compactMapper;

    public JsonService() {
        this.prettyMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.compactMapper = new ObjectMapper();
    }

    // ─── FORMAT ────────────────────────────────────────────────────────────────

    /**
     * Pretty-print or minify the given raw JSON string.
     * Throws IllegalArgumentException if the input is not valid JSON.
     */
    public String format(String rawJson, boolean pretty) throws Exception {
        // Parse first to validate, then re-serialize
        JsonNode node = compactMapper.readTree(rawJson);
        return pretty
                ? prettyMapper.writeValueAsString(node)
                : compactMapper.writeValueAsString(node);
    }

    // ─── COMPARE ───────────────────────────────────────────────────────────────

    /**
     * Recursively walks both JSON trees and collects paths where values differ.
     * Reports keys missing in one side as null vs. actual value.
     */
    public List<Difference> compare(String rawJson1, String rawJson2) throws JsonProcessingException {
        JsonNode json1 = compactMapper.readTree(rawJson1);
        JsonNode json2 = compactMapper.readTree(rawJson2);
        List<Difference> diffs = new ArrayList<>();
        compareNodes("$", json1, json2, diffs);
        return diffs;
    }

    private void compareNodes(String path, JsonNode n1, JsonNode n2, List<Difference> diffs) {
        // Both null / missing → equal
        if (n1 == null && n2 == null) return;

        // One side is missing
        if (n1 == null || n2 == null) {
            diffs.add(new Difference(path, nodeToValue(n1), nodeToValue(n2)));
            return;
        }

        // Different node types (e.g. object vs array)
        if (!n1.getNodeType().equals(n2.getNodeType())) {
            diffs.add(new Difference(path, nodeToValue(n1), nodeToValue(n2)));
            return;
        }

        if (n1.isObject()) {
            // Gather all keys from both sides
            Set<String> keys = new LinkedHashSet<>();
            n1.fieldNames().forEachRemaining(keys::add);
            n2.fieldNames().forEachRemaining(keys::add);

            for (String key : keys) {
                compareNodes(path + "." + key, n1.get(key), n2.get(key), diffs);
            }
        } else if (n1.isArray()) {
            int maxLen = Math.max(n1.size(), n2.size());
            for (int i = 0; i < maxLen; i++) {
                compareNodes(
                        path + "[" + i + "]",
                        i < n1.size() ? n1.get(i) : null,
                        i < n2.size() ? n2.get(i) : null,
                        diffs
                );
            }
        } else {
            // Leaf value — compare text representations
            if (!n1.asText().equals(n2.asText())) {
                diffs.add(new Difference(path, nodeToValue(n1), nodeToValue(n2)));
            }
        }
    }

    /** Convert a JsonNode leaf to a plain Java value for clean JSON serialization. */
    private Object nodeToValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isBoolean()) return node.asBoolean();
        if (node.isInt())     return node.asInt();
        if (node.isLong())    return node.asLong();
        if (node.isDouble())  return node.asDouble();
        return node.asText();
    }

    // ─── SEARCH ────────────────────────────────────────────────────────────────

    /**
     * Case-insensitive search through all keys and values in the JSON tree.
     * Returns every leaf node whose key OR value contains the query string.
     */
    public List<Match> search(String rawJson, String query) throws JsonProcessingException {
        JsonNode json = compactMapper.readTree(rawJson);

        List<Match> matches = new ArrayList<>();
        searchNodes("$", json, query.toLowerCase(), matches);
        return matches;
    }

    private void searchNodes(String path, JsonNode node, String query, List<Match> matches) {
        if (node == null) return;

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String childPath = path + "." + entry.getKey();

                // Match on key name
                if (entry.getKey().toLowerCase().contains(query)) {
                    matches.add(new Match(childPath, nodeToValue(entry.getValue())));
                }
                searchNodes(childPath, entry.getValue(), query, matches);
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                searchNodes(path + "[" + i + "]", node.get(i), query, matches);
            }
        } else {
            // Leaf — match on value
            if (node.asText().toLowerCase().contains(query)) {
                matches.add(new Match(path, nodeToValue(node)));
            }
        }
    }

    // ─── EXTRACT (JSONPath) ────────────────────────────────────────────────────

    /**
     * Evaluate a JSONPath expression against the JSON.
     * Always returns a List so the response shape is consistent.
     */
    public List<Object> extract(String rawJson, String jsonPath) throws Exception {
        // Configure JsonPath to always return a list
        JsonNode json = compactMapper.readTree(rawJson);

        Configuration config = Configuration.defaultConfiguration()
                .addOptions(Option.ALWAYS_RETURN_LIST);

        String jsonString = compactMapper.writeValueAsString(json);
        Object result = JsonPath.using(config).parse(jsonString).read(jsonPath);

        if (result instanceof List) {
            return (List<Object>) result;
        }
        // Wrap single value in a list
        return Collections.singletonList(result);
    }
}
