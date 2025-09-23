package org.example;

import java.util.HashMap;
import java.util.Map;

public class ParsedMessage {
    private String tag;
    private String date;
    private Map<String, String> params = new HashMap<>();


    public ParsedMessage(String tag, String date, Map<String, String> params) {
        this.tag = tag;
        this.date = date;
        this.params = params;
    }

    public ParsedMessage() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
