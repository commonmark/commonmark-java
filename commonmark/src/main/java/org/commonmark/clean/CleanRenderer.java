package org.commonmark.clean;

import org.commonmark.node.Node;

public class CleanRenderer {
    public String render(Node node) {
        StringBuilder ssb = new StringBuilder();
        render(node, ssb);
        return ssb.toString();
    }

    public void render(Node node, StringBuilder ssb) {
        CleanVisitor visitor = new CleanVisitor(new CleanWriter(ssb));
        node.accept(visitor);
    }
}
