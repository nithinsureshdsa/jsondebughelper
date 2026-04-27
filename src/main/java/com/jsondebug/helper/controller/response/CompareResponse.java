package com.jsondebug.helper.controller.response;

import java.util.List;

public class CompareResponse {
    private List<Difference> differences;

    public CompareResponse(List<Difference> differences) {
        this.differences = differences;
    }

    public List<Difference> getDifferences() { return differences; }
    public void setDifferences(List<Difference> differences) { this.differences = differences; }

    // Inner class for a single diff entry
    public static class Difference {
        private String path;
        private Object value1;
        private Object value2;

        public Difference(String path, Object value1, Object value2) {
            this.path = path;
            this.value1 = value1;
            this.value2 = value2;
        }

        public String getPath() { return path; }
        public Object getValue1() { return value1; }
        public Object getValue2() { return value2; }
    }
}
