package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

import java.util.List;

public class ParagraphParser extends AbstractBlockParser {

    private final Paragraph block = new Paragraph();
    private LinkReferenceDefinitionParser linkReferenceDefinitionParser = new LinkReferenceDefinitionParser();

    @Override
    public boolean canHaveLazyContinuationLines() {
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (!state.isBlank()) {
            return BlockContinue.atIndex(state.getIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(CharSequence line) {
        linkReferenceDefinitionParser.parse(line);
    }

    @Override
    public void closeBlock() {
        if (linkReferenceDefinitionParser.getParagraphContent().length() == 0) {
            block.unlink();
        }
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        CharSequence content = linkReferenceDefinitionParser.getParagraphContent();
        if (content.length() > 0) {
            inlineParser.parse(content.toString(), block);
        }
    }

    public CharSequence getContentString() {
        return linkReferenceDefinitionParser.getParagraphContent();
    }

    public List<LinkReferenceDefinition> getDefinitions() {
        return linkReferenceDefinitionParser.getDefinitions();
    }
}
