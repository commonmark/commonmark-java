package org.commonmark.parser;

import org.commonmark.node.Node;

import java.util.List;

/**
 * Parser for inline content (text, links, emphasized text, etc).
 */
public interface InlineParser {

    /**
     * @param input the content to parse as inline
     * @param node the node to append resulting nodes to (as children)
     */
    void parse(String input, Node node);

    interface NodeExtension {
        List<InlineBreakdown> lookup(String inline);

        class InlineBreakdown {
            int startIndex;
            int endIndex;
            Node node;

            public InlineBreakdown(Node node) {
                this.node = node;
            }

            public Node getNode() {
                return this.node;
            }
        }
    }
}
