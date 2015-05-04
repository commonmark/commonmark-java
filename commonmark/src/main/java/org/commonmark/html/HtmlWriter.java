package org.commonmark.html;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class HtmlWriter {

    private static final Map<String, String> NO_ATTRIBUTES = Collections.emptyMap();
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");

    private final Appendable buffer;
    private int nesting = 0;
    private char lastChar = 0;

    public HtmlWriter(Appendable out) {
        this.buffer = out;
    }

    public void raw(String s) {
        if (isTagAllowed()) {
            append(s);
        } else {
            append(HTML_TAG_PATTERN.matcher(s).replaceAll(""));
        }
    }

    public boolean isTagAllowed() {
        return nesting == 0;
    }

    public void disableTags() {
        nesting++;
    }

    public void enableTags() {
        nesting--;
    }

    public void tag(String name) {
        tag(name, NO_ATTRIBUTES);
    }

    public void tag(String name, Map<String, String> attrs) {
        tag(name, attrs, false);
    }

    // Helper function to produce an HTML tag.
    public void tag(String name, Map<String, String> attrs, boolean voidElement) {
        if (!isTagAllowed()) {
            return;
        }

        append("<");
        append(name);
        if (attrs != null && !attrs.isEmpty()) {
            for (Map.Entry<String, String> attrib : attrs.entrySet()) {
                append(" ");
                append(attrib.getKey());
                append("=\"");
                append(attrib.getValue());
                append("\"");
            }
        }
        if (voidElement) {
            append(" /");
        }

        append(">");
    }

    public void line() {
        if (lastChar != 0 && lastChar != '\n') {
            append("\n");
        }
    }

    protected void append(String s) {
        try {
            buffer.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int length = s.length();
        if (length != 0) {
            lastChar = s.charAt(length - 1);
        }
    }
}
