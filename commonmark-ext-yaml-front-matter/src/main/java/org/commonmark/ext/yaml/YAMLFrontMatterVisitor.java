package org.commonmark.ext.yaml;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YAMLFrontMatterVisitor extends AbstractVisitor {
    private Map<String, List<String>> data;

    public YAMLFrontMatterVisitor() {
        data = new LinkedHashMap<>();
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof YAMLFrontMatterNode) {
            data.put(((YAMLFrontMatterNode) customNode).getKey(), ((YAMLFrontMatterNode) customNode).getValues());
        } else {
            super.visit(customNode);
        }
    }

    public Map<String, List<String>> getData() {
        return data;
    }
}
