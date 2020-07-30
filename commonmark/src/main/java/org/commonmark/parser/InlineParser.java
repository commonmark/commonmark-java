package org.commonmark.parser;

import org.commonmark.node.Node;

import java.util.List;

/**
 * Parser for inline content (text, links, emphasized text, etc).
 */
public interface InlineParser {

    /**
     * @param lines the content to parse as inline
     * @param node the node to append resulting nodes to (as children)
     */
    // TODO: Should we use a better type here, one that will be able to include source positions?
    void parse(List<CharSequence> lines, Node node);
}
