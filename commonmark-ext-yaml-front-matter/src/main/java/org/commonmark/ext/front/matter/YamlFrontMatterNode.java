package org.commonmark.ext.front.matter;

import java.util.List;
import org.commonmark.node.CustomNode;

public class YamlFrontMatterNode extends CustomNode {
    private String key;
    private List<String> values;

    public YamlFrontMatterNode(String key, List<String> values) {
        this.key = key;
        this.values = values;
    }

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
