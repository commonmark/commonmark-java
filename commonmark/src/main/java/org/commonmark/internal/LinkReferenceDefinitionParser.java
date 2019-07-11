package org.commonmark.internal;

import org.commonmark.internal.util.Escaping;
import org.commonmark.internal.util.LinkScanner;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.LinkReferenceDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for link reference definitions at the beginning of a paragraph.
 *
 * @see <a href="https://spec.commonmark.org/0.29/#link-reference-definition">Link reference definitions</a>
 */
public class LinkReferenceDefinitionParser {

    private State state = State.START_DEFINITION;

    private final StringBuilder paragraph = new StringBuilder();
    private final List<LinkReferenceDefinition> definitions = new ArrayList<>();

    private StringBuilder label;
    private String normalizedLabel;
    private String destination;
    private char titleDelimiter;
    private StringBuilder title;
    private boolean referenceValid = false;

    public void parse(CharSequence line) {
        if (paragraph.length() != 0) {
            paragraph.append('\n');
        }
        paragraph.append(line);

        int i = 0;
        while (i < line.length()) {
            switch (state) {
                case PARAGRAPH: {
                    // We're in a paragraph now. Link reference definitions can only appear at the beginning, so once
                    // we're in a paragraph, there's no going back.
                    return;
                }
                case START_DEFINITION: {
                    i = startDefinition(line, i);
                    break;
                }
                case LABEL: {
                    i = label(line, i);
                    break;
                }
                case DESTINATION: {
                    i = destination(line, i);
                    break;
                }
                case START_TITLE: {
                    i = startTitle(line, i);
                    break;
                }
                case TITLE: {
                    i = title(line, i);
                    break;
                }
            }
            // -1 is returned if parsing failed, which means we fall back to treating text as a paragraph.
            if (i == -1) {
                state = State.PARAGRAPH;
                return;
            }
        }
    }

    CharSequence getParagraphContent() {
        return paragraph;
    }

    List<LinkReferenceDefinition> getDefinitions() {
        finishReference();
        return definitions;
    }

    State getState() {
        return state;
    }

    private int startDefinition(CharSequence line, int i) {
        i = Parsing.skipSpaceTab(line, i, line.length());
        if (i >= line.length() || line.charAt(i) != '[') {
            return -1;
        }

        state = State.LABEL;
        label = new StringBuilder();

        int labelStart = i + 1;
        if (labelStart >= line.length()) {
            label.append('\n');
        }

        return labelStart;
    }

    private int label(CharSequence line, int i) {
        int afterLabel = LinkScanner.scanLinkLabelContent(line, i);
        if (afterLabel == -1) {
            return -1;
        }

        label.append(line, i, afterLabel);

        if (afterLabel >= line.length()) {
            // label might continue on next line
            label.append('\n');
            return afterLabel;
        } else if (line.charAt(afterLabel) == ']') {
            int colon = afterLabel + 1;
            // end of label
            if (colon >= line.length() || line.charAt(colon) != ':') {
                return -1;
            }

            // spec: A link label can have at most 999 characters inside the square brackets.
            if (label.length() > 999) {
                return -1;
            }

            String normalizedLabel = Escaping.normalizeLabelContent(label.toString());
            if (normalizedLabel.isEmpty()) {
                return -1;
            }

            this.normalizedLabel = normalizedLabel;
            state = State.DESTINATION;

            return Parsing.skipSpaceTab(line, colon + 1, line.length());
        } else {
            return -1;
        }
    }

    private int destination(CharSequence line, int i) {
        i = Parsing.skipSpaceTab(line, i, line.length());
        int afterDestination = LinkScanner.scanLinkDestination(line, i);
        if (afterDestination == -1) {
            return -1;
        }

        destination = (line.charAt(i) == '<')
                ? line.subSequence(i + 1, afterDestination - 1).toString()
                : line.subSequence(i, afterDestination).toString();

        int afterSpace = Parsing.skipSpaceTab(line, afterDestination, line.length());
        if (afterSpace >= line.length()) {
            // Destination was at end of line, so this is a valid reference for sure (and maybe a title).
            // If not at end of line, wait for title to be valid first.
            referenceValid = true;
            paragraph.setLength(0);
        } else if (afterSpace == afterDestination) {
            // spec: The title must be separated from the link destination by whitespace
            return -1;
        }

        state = State.START_TITLE;
        return afterSpace;
    }

    private int startTitle(CharSequence line, int i) {
        i = Parsing.skipSpaceTab(line, i, line.length());
        if (i >= line.length()) {
            state = State.START_DEFINITION;
            return i;
        }

        titleDelimiter = '\0';
        char c = line.charAt(i);
        switch (c) {
            case '"':
            case '\'':
                titleDelimiter = c;
                break;
            case '(':
                titleDelimiter = ')';
                break;
        }

        if (titleDelimiter != '\0') {
            state = State.TITLE;
            title = new StringBuilder();
            i++;
            if (i == line.length()) {
                title.append('\n');
            }
        } else {
            finishReference();
            // There might be another reference instead, try that for the same character.
            state = State.START_DEFINITION;
        }
        return i;
    }

    private int title(CharSequence line, int i) {
        int afterTitle = LinkScanner.scanLinkTitleContent(line, i, titleDelimiter);
        if (afterTitle == -1) {
            // Invalid title, stop
            return -1;
        }

        title.append(line.subSequence(i, afterTitle));

        if (afterTitle >= line.length()) {
            // Title still going, continue on next line
            title.append('\n');
            return afterTitle;
        }

        int afterTitleDelimiter = afterTitle + 1;
        int afterSpace = Parsing.skipSpaceTab(line, afterTitleDelimiter, line.length());
        if (afterSpace != line.length()) {
            // spec: No further non-whitespace characters may occur on the line.
            return -1;
        }
        referenceValid = true;
        finishReference();
        paragraph.setLength(0);

        // See if there's another definition.
        state = State.START_DEFINITION;
        return afterSpace;
    }

    private void finishReference() {
        if (!referenceValid) {
            return;
        }

        String d = Escaping.unescapeString(destination);
        String t = title != null ? Escaping.unescapeString(title.toString()) : null;
        definitions.add(new LinkReferenceDefinition(normalizedLabel, d, t));

        label = null;
        referenceValid = false;
        normalizedLabel = null;
        destination = null;
        title = null;
    }

    enum State {
        // Looking for the start of a definition, i.e. `[`
        START_DEFINITION,
        // Parsing the label, i.e. `foo` within `[foo]`
        LABEL,
        // Parsing the destination, i.e. `/url` in `[foo]: /url`
        DESTINATION,
        // Looking for the start of a title, i.e. the first `"` in `[foo]: /url "title"`
        START_TITLE,
        // Parsing the content of the title, i.e. `title` in `[foo]: /url "title"`
        TITLE,

        // End state, no matter what kind of lines we add, they won't be references
        PARAGRAPH,
    }
}
