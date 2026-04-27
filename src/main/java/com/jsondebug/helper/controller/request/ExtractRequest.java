package com.jsondebug.helper.controller.request;

import com.fasterxml.jackson.databind.JsonNode;

public class ExtractRequest {
    private String json;
    private String path;

    public String getJson() { return json; }
    public void setJson(String json) { this.json = json; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
