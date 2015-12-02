package org.commonmark.ext.metadata;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MetadataVisitor extends AbstractVisitor {
    private Map<String, List<String>> data;

    public MetadataVisitor() {
        data = new LinkedHashMap<>();
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof MetadataNode) {
            data.put(((MetadataNode) customNode).getKey(), ((MetadataNode) customNode).getValues());
        } else {
            super.visit(customNode);
        }
    }

    public Map<String, List<String>> getData() {
        return data;
    }
}
