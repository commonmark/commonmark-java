package org.commonmark.ext.gfm.alerts.internal;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class AlertPostProcessor implements PostProcessor {

    // Alert type marker, matching any case (GitHub supports lowercase, mixed, and uppercase)
    private static final Pattern ALERT_PATTERN = Pattern.compile("^\\[!([a-zA-Z]+)]\\s*$");

    private final Set<String> allowedTypes;

    public AlertPostProcessor(Set<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    @Override
    public Node process(Node document) {
        // Only look at direct children of Document — GitHub only detects alerts at the top level.
        var child = document.getFirstChild();
        while (child != null) {
            var next = child.getNext();
            if (child instanceof BlockQuote) {
                tryConvertToAlert((BlockQuote) child);
            }
            child = next;
        }
        return document;
    }

    private void tryConvertToAlert(BlockQuote blockQuote) {
        var firstChild = blockQuote.getFirstChild();
        if (!(firstChild instanceof Paragraph)) {
            return;
        }

        var paragraph = (Paragraph) firstChild;
        var firstInline = paragraph.getFirstChild();
        if (!(firstInline instanceof Text)) {
            return;
        }

        var textNode = (Text) firstInline;

        // The alert marker can be the entire text node content, or just the first line
        // before a line break (trailing spaces create a HardLineBreak instead of SoftLineBreak).
        var afterMarker = firstInline.getNext();
        if (afterMarker != null && !(afterMarker instanceof SoftLineBreak) && !(afterMarker instanceof HardLineBreak)) {
            // Text followed by something other than a line break - not an alert
            return;
        }

        var matcher = ALERT_PATTERN.matcher(textNode.getLiteral());
        if (!matcher.matches()) {
            return;
        }

        var type = matcher.group(1).toUpperCase(Locale.ROOT);
        if (!allowedTypes.contains(type)) {
            return;
        }

        // Must have content after the marker line. An alert with ONLY the marker
        // and no content is a normal blockquote on GitHub.
        if (afterMarker != null) {
            // There's a line break after marker - check if there's content after it
            if (afterMarker.getNext() == null && paragraph.getNext() == null) {
                return;
            }
            afterMarker.unlink();
        } else {
            // Marker is the only thing in the paragraph
            if (paragraph.getNext() == null) {
                return;
            }
        }

        // Valid alert. Create Alert node and transfer children.
        var alert = new Alert(type);
        alert.setSourceSpans(blockQuote.getSourceSpans());
        blockQuote.insertAfter(alert);

        // Remove the marker text from the first paragraph
        firstInline.unlink();

        // If paragraph is now empty, remove it
        if (paragraph.getFirstChild() == null) {
            paragraph.unlink();
        }

        // Move remaining children from blockquote to alert
        var child = blockQuote.getFirstChild();
        while (child != null) {
            var next = child.getNext();
            alert.appendChild(child);
            child = next;
        }

        blockQuote.unlink();
    }
}
