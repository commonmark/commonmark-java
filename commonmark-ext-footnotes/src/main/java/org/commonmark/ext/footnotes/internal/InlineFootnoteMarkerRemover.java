package org.commonmark.ext.footnotes.internal;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.CustomNode;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

/**
 * Remove any markers that have been parsed by {@link InlineFootnoteMarkerParser} but haven't been used in
 * {@link FootnoteLinkProcessor}.
 */
public class InlineFootnoteMarkerRemover extends AbstractVisitor implements PostProcessor {
    @Override
    public Node process(Node node) {
        node.accept(this);
        return node;
    }

    @Override
    public void visit(CustomNode customNode) {
        if (customNode instanceof InlineFootnoteMarker) {
            var text = new Text("^");
            text.setSourceSpans(customNode.getSourceSpans());
            customNode.insertAfter(text);
            customNode.unlink();
        }
    }
}
