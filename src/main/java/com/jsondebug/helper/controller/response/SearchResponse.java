package com.jsondebug.helper.controller.response;

import java.util.List;

public class SearchResponse {
    private List<Match> matches;

    public SearchResponse(List<Match> matches) {
        this.matches = matches;
    }

    public List<Match> getMatches() { return matches; }
    public void setMatches(List<Match> matches) { this.matches = matches; }

    public static class Match {
        private String path;
        private Object value;

        public Match(String path, Object value) {
            this.path = path;
            this.value = value;
        }

        public String getPath() { return path; }
        public Object getValue() { return value; }
    }
}
