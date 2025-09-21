package org.example;

import java.util.HashMap;
import java.util.Map;

public class ParsedMessage {
        private String tag;
        private String initialDate;
        private int paramCount;
        private Map<String, String> params = new HashMap<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getInitialDate() {
        return initialDate;
    }

    public void setInitialDate(String initialDate) {
        this.initialDate = initialDate;
    }

    public int getParamCount() {
        return paramCount;
    }

    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
