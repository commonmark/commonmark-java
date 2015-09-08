package org.commonmark.ext.replacement;

import org.commonmark.Extension;
import org.commonmark.ext.replacement.internal.ReplacementPostProcessor;
import org.commonmark.parser.Parser;

import java.util.Map;

/**
 * Extension for automatically turning plain URLs into links.
 * <p>
 * Create it with {@link #create()} and then configure it on the builder
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed links are turned into normal {@link org.commonmark.node.Link} nodes.
 * </p>
 */
public class ReplacementExtension implements Parser.ParserExtension {
    private final Map<String, String> mReplacementMap;

    private ReplacementExtension(Map<String, String> replacementMap) {
        mReplacementMap = replacementMap;
    }

    public static Extension create(Map<String, String> replacementMap) {
        return new ReplacementExtension(replacementMap);
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.postProcessor(new ReplacementPostProcessor(mReplacementMap));
    }
}
