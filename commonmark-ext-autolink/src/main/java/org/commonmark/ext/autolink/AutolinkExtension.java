package org.commonmark.ext.autolink;

import org.commonmark.parser.Parser;

public class AutolinkExtension implements Parser.ParserExtension {

    private AutolinkExtension() {
    }

    public static AutolinkExtension create() {
        return new AutolinkExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.postProcessor(new AutolinkPostProcessor());
    }

}
