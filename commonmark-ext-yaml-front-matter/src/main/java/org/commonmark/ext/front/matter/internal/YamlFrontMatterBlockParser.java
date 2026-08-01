package org.commonmark.ext.front.matter.internal;

import java.util.regex.Pattern;

import org.commonmark.ext.front.matter.YamlFrontMatterExtractor;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.node.Block;
import org.commonmark.node.Document;
import org.commonmark.parser.block.*;

public class YamlFrontMatterBlockParser extends AbstractBlockParser {
    private static final Pattern REGEX_BEGIN = Pattern.compile("^-{3}(\\s.*)?");
    private static final Pattern REGEX_END = Pattern.compile("^(-{3}|\\.{3})(\\s.*)?");

    private YamlFrontMatterBlock block;
    private YamlFrontMatterExtractor extractor;

    public YamlFrontMatterBlockParser(YamlFrontMatterExtractor extractor) {
        this.extractor = extractor;
        block = new YamlFrontMatterBlock();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        final CharSequence line = parserState.getLine().getContent();

        if (REGEX_END.matcher(line).matches()) {
            return extractor.onBlockEnd(block);
        }

        extractor.onNextLine(block, line);
        return BlockContinue.atIndex(parserState.getIndex());
    }

    public static class Factory extends AbstractBlockParserFactory {
        private YamlFrontMatterExtractor.Factory yamlExtractorFactory;

        public Factory(YamlFrontMatterExtractor.Factory factory) {
            this.yamlExtractorFactory = factory;
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine().getContent();
            BlockParser parentParser = matchedBlockParser.getMatchedBlockParser();
            // check whether this line is the first line of whole document or not
            if (parentParser.getBlock() instanceof Document
                    && parentParser.getBlock().getFirstChild() == null
                    && REGEX_BEGIN.matcher(line).matches()) {
                return BlockStart.of(new YamlFrontMatterBlockParser(yamlExtractorFactory.create()))
                        .atIndex(state.getNextNonSpaceIndex());
            }

            return BlockStart.none();
        }
    }
}
