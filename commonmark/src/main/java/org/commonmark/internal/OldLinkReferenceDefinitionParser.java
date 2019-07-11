package org.commonmark.internal;

import org.commonmark.internal.util.Escaping;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @see <a href="https://spec.commonmark.org/0.29/#link-reference-definition">Link reference definitions</a>
 */
class OldLinkReferenceDefinitionParser extends InlineParserImpl {

    /**
     * Parsed link reference definitions by label, in order of occurrence.
     */
    private List<LinkReferenceDefinition> definitions = new ArrayList<>();

    private static final Pattern LINE_END = Pattern.compile("^ *(?:\n|$)");

    OldLinkReferenceDefinitionParser() {
        // Not needed for parsing link reference definitions
        super(new InlineParserContextImpl(Collections.<DelimiterProcessor>emptyList(),
                Collections.<String, LinkReferenceDefinition>emptyMap()));
    }

    // TODO: Would be better to just return them from the method.
    List<LinkReferenceDefinition> getDefinitions() {
        return definitions;
    }

    /**
     * Parse all link reference definitions, add them to the map and return the length of the text we parsed (if any).
     */
    int parseDefinitions(String content) {
        int afterAllDefinitions = 0;
        while (content.length() > 3 && content.charAt(0) == '[') {
            int afterDefinition = parseDefinition(content);
            if (afterDefinition != 0) {
                content = content.substring(afterDefinition);
                afterAllDefinitions += afterDefinition;
            } else {
                break;
            }
        }
        return afterAllDefinitions;
    }

    /**
     * Attempt to parse a single link reference definition, adding it to the map.
     */
    private int parseDefinition(String content) {
        reset(content);

        String dest;
        String title = null;
        int matchChars;
        int startIndex = index;

        // label:
        matchChars = parseLinkLabel();
        if (matchChars == 0) {
            return 0;
        }

        String rawLabel = input.substring(0, matchChars);

        // colon:
        if (peek() != ':') {
            return 0;
        }
        index++;

        // link url
        spnl();

        dest = parseLinkDestination();
        if (dest == null) {
            return 0;
        }

        int beforeTitle = index;
        spnl();
        if (index != beforeTitle) {
            title = parseLinkTitle();
        }
        if (title == null) {
            // rewind before spaces
            index = beforeTitle;
        }

        boolean atLineEnd = true;
        if (index != input.length() && match(LINE_END) == null) {
            if (title == null) {
                atLineEnd = false;
            } else {
                // the potential title we found is not at the line end,
                // but it could still be a legal link reference if we
                // discard the title
                title = null;
                // rewind before spaces
                index = beforeTitle;
                // and instead check if the link URL is at the line end
                atLineEnd = match(LINE_END) != null;
            }
        }

        if (!atLineEnd) {
            return 0;
        }

        String normalizedLabel = Escaping.normalizeReference(rawLabel);
        if (normalizedLabel.isEmpty()) {
            return 0;
        }

        definitions.add(new LinkReferenceDefinition(normalizedLabel, dest, title));

        return index - startIndex;
    }
}
