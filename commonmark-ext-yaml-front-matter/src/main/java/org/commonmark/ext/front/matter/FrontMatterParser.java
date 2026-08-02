package org.commonmark.ext.front.matter;

import org.commonmark.parser.SourceLine;

/**
 * Parses the content of the front matter block between `---` separators.
 * The implementations should add at least 1 child node to
 * {@link YamlFrontMatterBlock} to store the result of the parsing.
 */
public interface FrontMatterParser {
    void onNextLine(YamlFrontMatterBlock block, SourceLine line);

    /**
     * Notifies about finding the line with the ending separator (`---` or `...`, without
     * initial whitespace). Advanced parsers may be able to determine that the separator
     * is a part of the single-/double-quoted multiline string and return {@link SeparatorRole#CONTENT}
     * to include it in the front matter content. In this case, front matter parsing continues until
     * finding another separator. To end the parsing, return {@link SeparatorRole#BLOCK_END}.
     *
     * @param block Main block node
     * @param separator `---` or `...`
     * @return Separator role: block end or the part of the front matter content
     */
    SeparatorRole onEndingSeparator(YamlFrontMatterBlock block, SourceLine separator);

    interface Factory {
        FrontMatterParser create();
    }

    enum SeparatorRole {
        BLOCK_END, CONTENT
    }
}
