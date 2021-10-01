package org.commonmark.internal;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.internal.inline.Position;
import org.commonmark.internal.inline.Scanner;
import org.commonmark.internal.util.Escaping;
import org.commonmark.internal.util.LinkScanner;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;

/**
 * Parser for link reference definitions at the beginning of a paragraph.
 *
 * @see <a href="https://spec.commonmark.org/0.29/#link-reference-definition">Link reference definitions</a>
 */
public class LinkReferenceDefinitionParser {

    private State state = State.START_DEFINITION;

    private final List<SourceLine> paragraphLines = new ArrayList<>();
    private final List<LinkReferenceDefinition> definitions = new ArrayList<>();
    private final List<SourceSpan> sourceSpans = new ArrayList<>();

    private StringBuilder label;
    private String extendedLabel = null;
    private String destination;
    private String rawDestination;
    private char titleDelimiter;
    private StringBuilder title;
    private boolean referenceValid = false;
    private List<SourceLine> rawParagraphLines = new ArrayList<>();
    
    private int previousLineIndex = 0;
    private String whitespacePreLabel = "";
    private String whitespacePreDestination = "";
    private String whitespacePreTitle = "";
    private String whitespacePostTitle = "";

    public void parse(SourceLine line) {
        paragraphLines.add(line.getLiteralLine());
        rawParagraphLines.add(line);
        
        if (state == State.PARAGRAPH) {
            // We're in a paragraph now. Link reference definitions can only appear at the beginning, so once
            // we're in a paragraph, there's no going back.
            return;
        }

        Scanner scanner = Scanner.of(SourceLines.of(line));
        
        while (scanner.hasNext()) {
            boolean success;
            switch (state) {
                case START_DEFINITION: {
                    success = startDefinition(scanner);
                    break;
                }
                case LABEL: {
                    // Capture line number to determine if line breaks occur
                    if(line.getSourceSpan() != null) {
                        previousLineIndex = line.getSourceSpan().getLineIndex();
                    }
                    
                    success = label(scanner);
                    break;
                }
                case DESTINATION: {
                    // Check for any line breaks
                    if(line.getSourceSpan() != null) {
                        if(line.getSourceSpan().getLineIndex() > previousLineIndex) {
                            whitespacePreDestination = whitespacePreDestination + "\n";
                            previousLineIndex++;
                        }
                    }
                    
                    success = destination(scanner);
                    break;
                }
                case START_TITLE: {
                    // Check for any line breaks
                    if(line.getSourceSpan() != null) {
                        if(line.getSourceSpan().getLineIndex() > previousLineIndex) {
                            whitespacePreTitle = whitespacePreTitle + "\n";
                            previousLineIndex++;
                        }
                    }
                    
                    success = startTitle(scanner);
                    break;
                }
                case TITLE: {
                    success = title(scanner);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown parsing state: " + state);
                }
            }
            // Parsing failed, which means we fall back to treating text as a paragraph.
            if (!success) {
                state = State.PARAGRAPH;
                return;
            }
        }
    }

    public void addSourceSpan(SourceSpan sourceSpan) {
        sourceSpans.add(sourceSpan);
    }

    /**
     * @return the lines that are normal paragraph content, without newlines
     */
    SourceLines getParagraphLines() {
        return SourceLines.of(paragraphLines);
    }
    
    SourceLines getRawParagraphLines() {
        return SourceLines.of(rawParagraphLines);
    }
    
    void setRawParagraphLines(SourceLines rawParagraphLines) {
        this.rawParagraphLines = rawParagraphLines.getLines();
    }

    List<SourceSpan> getParagraphSourceSpans() {
        return sourceSpans;
    }
    
    String getExtendedLabel() {
        return extendedLabel;
    }

    List<LinkReferenceDefinition> getDefinitions() {
        finishReference();
        return definitions;
    }

    State getState() {
        return state;
    }

    private boolean startDefinition(Scanner scanner) {
        // Capture whitespace for roundtrip purposes
        whitespacePreLabel = scanner.whitespaceAsString();
        
        String whitespaceCheck = scanner.alignToLiteral();
        
        if(!whitespaceCheck.isEmpty()) {
            whitespacePreLabel = whitespaceCheck;
        }
        
        if (!scanner.next('[')) {
            return false;
        }

        state = State.LABEL;
        label = new StringBuilder();

        if (!scanner.hasNext()) {
            label.append('\n');
        }
        return true;
    }

    private boolean label(Scanner scanner) {
        Position start = scanner.position();
        if (!LinkScanner.scanLinkLabelContent(scanner)) {
            return false;
        }

        label.append(scanner.getSource(start, scanner.position()).getContent());

        if (!scanner.hasNext()) {
            // label might continue on next line
            label.append('\n');
            return true;
        } else if (scanner.next(']')) {
            // end of label
            if (!scanner.next(':')) {
                return false;
            }

            // spec: A link label can have at most 999 characters inside the square brackets.
            if (label.length() > 999) {
                return false;
            }

            String normalizedLabel = Escaping.normalizeLabelContent(label.toString());
            if (normalizedLabel.isEmpty()) {
                return false;
            }

            state = State.DESTINATION;

            whitespacePreDestination = scanner.whitespaceAsString();
            return true;
        } else {
            return false;
        }
    }

    private boolean destination(Scanner scanner) {
        whitespacePreDestination = whitespacePreDestination +
                scanner.whitespaceAsString();
        Position start = scanner.position();
        if (!LinkScanner.scanLinkDestination(scanner)) {
            return false;
        }

        rawDestination = scanner.getSource(start, scanner.position()).getContent();
        destination = rawDestination.startsWith("<") ?
                rawDestination.substring(1, rawDestination.length() - 1) :
                rawDestination;

        whitespacePreTitle = scanner.whitespaceAsString();
        if (!scanner.hasNext()) {
            // Destination was at end of line, so this is a valid reference for sure (and maybe a title).
            // If not at end of line, wait for title to be valid first.
            referenceValid = true;
            paragraphLines.clear();
            rawParagraphLines.clear();
        } else if (whitespacePreTitle.length() == 0) {
            // spec: The title must be separated from the link destination by whitespace
            return false;
        }

        state = State.START_TITLE;
        return true;
    }

    private boolean startTitle(Scanner scanner) {
        whitespacePreTitle = whitespacePreTitle + scanner.whitespaceAsString();
        if (!scanner.hasNext()) {
            state = State.START_DEFINITION;
            return true;
        }

        titleDelimiter = '\0';
        char c = scanner.peek();
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
            scanner.next();
            if (!scanner.hasNext()) {
                title.append('\n');
            }
        } else {
            finishReference();
            // There might be another reference instead, try that for the same character.
            state = State.START_DEFINITION;
        }
        return true;
    }

    private boolean title(Scanner scanner) {
        Position start = scanner.position();
        if (!LinkScanner.scanLinkTitleContent(scanner, titleDelimiter)) {
            // Invalid title, stop
            return false;
        }

        title.append(scanner.getSource(start, scanner.position()).getContent());

        if (!scanner.hasNext()) {
            // Title ran until the end of line, so continue on next line (until we find the delimiter)
            title.append('\n');
            return true;
        }

        // Skip delimiter character
        scanner.next();
        whitespacePostTitle = scanner.whitespaceAsString();
        if (scanner.hasNext()) {
            title = null;
            whitespacePostTitle = "";
            // spec: No further non-whitespace characters may occur on the line.
            return false;
        }
        referenceValid = true;
        finishReference();
        paragraphLines.clear();
        rawParagraphLines.clear();

        // See if there's another definition.
        state = State.START_DEFINITION;
        return true;
    }

    private void finishReference() {
        if (!referenceValid) {
            return;
        }

        if(rawDestination.isEmpty()) {
            rawDestination = destination;
        }

        String d = Escaping.unescapeString(destination);
        String t = title != null ? Escaping.unescapeString(title.toString()) : null;
        String rawTitle = title != null ? title.toString() : "";
        LinkReferenceDefinition definition = new LinkReferenceDefinition(label.toString(), d, rawDestination, t, rawTitle, titleDelimiter, whitespacePreLabel, whitespacePreDestination, whitespacePreTitle, whitespacePostTitle);
        definition.setSourceSpans(sourceSpans);
        sourceSpans.clear();
        definitions.add(definition);

        label = null;
        referenceValid = false;
        destination = null;
        title = null;
        whitespacePreDestination = "";
        whitespacePostTitle = "";
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
