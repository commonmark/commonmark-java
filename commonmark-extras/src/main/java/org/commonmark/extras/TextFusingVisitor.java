package org.commonmark.extras;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

/**
 * Visitor that "fuses" runs of text nodes together into a single text node.
 * <p/>
 * This is useful for doing post-processing on text nodes without having to worry about them being in lots of little
 * pieces.
 */
// TODO: Could this be done directly in inline parser instead (as we go)?
public class TextFusingVisitor extends AbstractVisitor {
    @Override
    protected void visitChildren(Node node) {
        Text previousText = null;
        StringBuilder sb = null;
        Node child = node.getFirstChild();
        while (child != null) {
            if (child instanceof Text) {
                Text text = (Text) child;
                if (previousText != null) {
                    if (sb == null) {
                        sb = new StringBuilder(previousText.getLiteral().length() + text.getLiteral().length() + 16);
                    }
                    sb.append(previousText.getLiteral());
                    previousText.unlink();
                }
                previousText = text;
            } else {
                if (previousText != null) {
                    if (sb != null) {
                        sb.append(previousText.getLiteral());
                        previousText.setLiteral(sb.toString());
                        sb = null;
                    }
                    previousText = null;
                }
            }
            child = child.getNext();
        }
        if (previousText != null && sb != null) {
            sb.append(previousText.getLiteral());
            previousText.setLiteral(sb.toString());
        }

        super.visitChildren(node);
    }
}
