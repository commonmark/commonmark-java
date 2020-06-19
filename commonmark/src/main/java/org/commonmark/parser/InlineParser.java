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

    /**
     *  Extension for inline content
     */
    interface NodeExtension {
        /**
         * The listing contains the all nodes that should be applied to this current line with the respective line positions
         *
         * @param input is the content to be parsed as custom nodes
         * @return list of nodes with their location in the current line
         */
        List<InlineBreakdown> lookup(String input);

        class InlineBreakdown {
            public static final InlineBreakdown EMPTY =
                    new InlineBreakdown(null, Integer.MAX_VALUE, Integer.MAX_VALUE);
            private final Node node;
            private final int beginIndex;
            private final int endIndex;

            private InlineBreakdown(Node node, int beginIndex, int endIndex) {
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


            /**
             * Inline Object to provide the {@code node} and node's line position
             * @param node such as the {@code Image}, {@code Link}, etc.
             * @param beginIndex as String.subString usage, the beginning index, inclusive.
             * @param endIndex as String.subString usage, the ending index, exclusive.
             * @exception  IllegalArgumentException  if the {@code endIndex} < {@code beginIndex}
             * @return {@code InlineBreakdown} built,
             *          if {@code beginIndex} == {@code endIndex} it will considered empty object
             */
            public static InlineBreakdown of(Node node, int beginIndex, int endIndex) {
                if (endIndex < beginIndex){
                    throw new IllegalArgumentException("beginIndex "+ beginIndex +" must be greater then endIndex " + endIndex);
                }

                if (beginIndex == endIndex) {
                    return EMPTY;
                }

                return new InlineBreakdown(node, beginIndex, endIndex);
            }
        }
    }
}
