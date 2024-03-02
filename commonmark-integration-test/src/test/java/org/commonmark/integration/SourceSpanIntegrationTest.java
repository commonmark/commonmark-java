package org.commonmark.integration;

import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.commonmark.testutil.example.Example;

/**
 * Spec and all extensions, with source spans enabled.
 */
public class SourceSpanIntegrationTest extends SpecIntegrationTest {

    protected static final Parser PARSER = Parser.builder()
            .extensions(Extensions.ALL_EXTENSIONS)
            .includeSourceSpans(IncludeSourceSpans.BLOCKS)
            .build();

    public SourceSpanIntegrationTest(Example example) {
        super(example);
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
