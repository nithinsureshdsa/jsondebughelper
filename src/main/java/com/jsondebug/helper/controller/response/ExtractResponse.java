package com.jsondebug.helper.controller.response;

import java.util.List;

public class ExtractResponse {
    private List<Object> result;

    public ExtractResponse(List<Object> result) {
        this.result = result;
    }

    public List<Object> getResult() { return result; }
    public void setResult(List<Object> result) { this.result = result; }
}
