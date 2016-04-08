package org.commonmark.ext.front.matter;

import org.commonmark.Extension;
import org.commonmark.ext.front.matter.internal.YamlFrontMatterBlockParser;
import org.commonmark.parser.Parser;

/**
 * Extension for YAML-like metadata.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link org.commonmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed metadata is turned into {@link YamlFrontMatterNode}. You can access the metadata using {@link YamlFrontMatterVisitor}.
 * </p>
 */
public class YamlFrontMatterExtension implements Parser.ParserExtension {

    private YamlFrontMatterExtension() {
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new YamlFrontMatterBlockParser.Factory());
    }

    public static Extension create() {
        return new YamlFrontMatterExtension();
    }
}
