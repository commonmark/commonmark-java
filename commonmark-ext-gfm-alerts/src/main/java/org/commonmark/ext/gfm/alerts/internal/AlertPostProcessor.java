package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.node.*;
import org.commonmark.parser.PostProcessor;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AlertPostProcessor implements PostProcessor {

    // Case-insensitive matching for alert type (GitHub supports any case)
    private static final Pattern ALERT_PATTERN = Pattern.compile("^\\[!([a-zA-Z]+)]\\s*$");

    private final Set<String> allowedTypes;

    public AlertPostProcessor(Set<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    @Override
    public Node process(Node document) {
        AlertVisitor visitor = new AlertVisitor(allowedTypes);
        document.accept(visitor);
        return document;
    }

    private static class AlertVisitor extends AbstractVisitor {

        private final Set<String> allowedTypes;

        AlertVisitor(Set<String> allowedTypes) {
            this.allowedTypes = allowedTypes;
        }

        @Override
        public void visit(BlockQuote blockQuote) {
            // Only convert top-level block quotes (direct children of Document).
            // This matches GitHub's behavior where alerts are only detected at the document level.
            if (blockQuote.getParent() instanceof Document) {
                if (tryConvertToAlert(blockQuote)) {
                    return;
                }
            }
            visitChildren(blockQuote);
        }

        private boolean tryConvertToAlert(BlockQuote blockQuote) {
            Node firstChild = blockQuote.getFirstChild();
            if (!(firstChild instanceof Paragraph)) {
                return false;
            }

            Paragraph paragraph = (Paragraph) firstChild;
            Node firstInline = paragraph.getFirstChild();
            if (!(firstInline instanceof Text)) {
                return false;
            }

            Text textNode = (Text) firstInline;
            String literal = textNode.getLiteral();

            // The alert marker can be the entire text node content, or just the first line
            // before a line break. We need to check both cases.
            // Trailing spaces on the marker line create a HardLineBreak instead of SoftLineBreak.
            String markerText;
            Node afterMarker = firstInline.getNext();
            if (afterMarker instanceof SoftLineBreak || afterMarker instanceof HardLineBreak || afterMarker == null) {
                markerText = literal;
            } else {
                // Text followed by something other than a line break on same line - not an alert
                return false;
            }

            Matcher matcher = ALERT_PATTERN.matcher(markerText);
            if (!matcher.matches()) {
                return false;
            }

            String type = matcher.group(1).toUpperCase();
            if (!allowedTypes.contains(type)) {
                return false;
            }

            // Must have content after the marker line. An alert with ONLY the marker
            // and no content is a normal blockquote on GitHub.
            boolean hasContentAfterMarker;
            if (afterMarker instanceof SoftLineBreak || afterMarker instanceof HardLineBreak) {
                // There's a line break after marker - check if there's content after it
                hasContentAfterMarker = afterMarker.getNext() != null || paragraph.getNext() != null;
            } else {
                // Marker is the only thing in this text node
                hasContentAfterMarker = paragraph.getNext() != null;
            }

            if (!hasContentAfterMarker) {
                return false;
            }

            // Check if the content after the marker is only whitespace
            if (isContentWhitespaceOnly(paragraph, firstInline)) {
                return false;
            }

            // Valid alert. Create Alert node and transfer children.
            Alert alert = new Alert(type);

            blockQuote.insertAfter(alert);

            // Remove the marker text and line break from the first paragraph
            if (afterMarker instanceof SoftLineBreak || afterMarker instanceof HardLineBreak) {
                afterMarker.unlink();
            }
            firstInline.unlink();

            // If paragraph is now empty, remove it
            if (paragraph.getFirstChild() == null) {
                paragraph.unlink();
            }

            // Move remaining children from blockquote to alert
            Node child = blockQuote.getFirstChild();
            while (child != null) {
                Node next = child.getNext();
                alert.appendChild(child);
                child = next;
            }

            blockQuote.unlink();
            return true;
        }

        private boolean isContentWhitespaceOnly(Paragraph firstParagraph, Node markerNode) {
            // Check inline nodes after the marker in the first paragraph
            Node next = markerNode.getNext();
            while (next != null) {
                if (next instanceof Text) {
                    if (!((Text) next).getLiteral().trim().isEmpty()) {
                        return false;
                    }
                } else if (!(next instanceof SoftLineBreak) && !(next instanceof HardLineBreak)) {
                    return false;
                }
                next = next.getNext();
            }

            // Check block-level siblings after the first paragraph
            Node block = firstParagraph.getNext();
            while (block != null) {
                if (block instanceof Paragraph) {
                    Node child = block.getFirstChild();
                    while (child != null) {
                        if (child instanceof Text && !((Text) child).getLiteral().trim().isEmpty()) {
                            return false;
                        } else if (!(child instanceof Text) && !(child instanceof SoftLineBreak) && !(child instanceof HardLineBreak)) {
                            return false;
                        }
                        child = child.getNext();
                    }
                } else {
                    return false;
                }
                block = block.getNext();
            }

            return true;
        }
    }
}
