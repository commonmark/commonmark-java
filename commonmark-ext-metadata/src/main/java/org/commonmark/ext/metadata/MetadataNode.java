package org.commonmark.ext.metadata;

import org.commonmark.node.CustomNode;

import java.util.List;

public class MetadataNode extends CustomNode {
    private String key;
    private List<String> values;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
