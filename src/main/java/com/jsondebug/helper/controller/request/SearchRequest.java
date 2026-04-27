package com.jsondebug.helper.controller.request;

import com.fasterxml.jackson.databind.JsonNode;

public class SearchRequest {
    private String json;
    private String query;

    public String getJson() { return json; }
    public void setJson(String json) { this.json = json; }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}