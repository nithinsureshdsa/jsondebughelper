package com.jsondebug.helper.controller.request;

public class FormatRequest {
    private String input;
    private boolean pretty = true;

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }

    public boolean isPretty() { return pretty; }
    public void setPretty(boolean pretty) { this.pretty = pretty; }
}
