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
            private final Node node;
            private final int beginIndex;
            private final int endIndex;

            public InlineBreakdown(Node node, int beginIndex, int endIndex) {
                this.node = node;
                this.beginIndex = beginIndex;
                this.endIndex = endIndex;
            }

            public Node getNode() {
                return this.node;
            }

            public int getBeginIndex() {
                return beginIndex;
            }

            public int getEndIndex() {
                return endIndex;
            }
        }
    }
}
