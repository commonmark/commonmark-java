package org.commonmark.ext.front.matter.internal;

import java.util.regex.Pattern;

import org.commonmark.ext.front.matter.FrontMatterParser;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.node.Block;
import org.commonmark.node.Document;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.*;

public class YamlFrontMatterBlockParser extends AbstractBlockParser {
    private static final Pattern REGEX_BEGIN = Pattern.compile("^-{3}(\\s.*)?");
    private static final Pattern REGEX_END = Pattern.compile("^(-{3}|\\.{3})(\\s.*)?");

    private YamlFrontMatterBlock block;
    private FrontMatterParser frontMatterParser;

    public YamlFrontMatterBlockParser(FrontMatterParser frontMatterParser) {
        this.frontMatterParser = frontMatterParser;
        block = new YamlFrontMatterBlock();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        final SourceLine line = parserState.getLine();

        if (REGEX_END.matcher(line.getContent()).matches()) {
            switch (frontMatterParser.onEndingSeparator(block, line)) {
                case BLOCK_END:
                    return BlockContinue.finished();
                case CONTENT:
                    return BlockContinue.atIndex(parserState.getIndex());
            }
        }

        frontMatterParser.onNextLine(block, line);
        return BlockContinue.atIndex(parserState.getIndex());
    }

    public static class Factory extends AbstractBlockParserFactory {
        private FrontMatterParser.Factory frontMatterParserFactory;

        public Factory(FrontMatterParser.Factory factory) {
            this.frontMatterParserFactory = factory;
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine().getContent();
            BlockParser parentParser = matchedBlockParser.getMatchedBlockParser();
            // check whether this line is the first line of whole document or not
            if (parentParser.getBlock() instanceof Document
                    && parentParser.getBlock().getFirstChild() == null
                    && REGEX_BEGIN.matcher(line).matches()) {
                return BlockStart.of(new YamlFrontMatterBlockParser(frontMatterParserFactory.create()))
                        .atIndex(state.getNextNonSpaceIndex());
            }

            return BlockStart.none();
        }
    }
}
