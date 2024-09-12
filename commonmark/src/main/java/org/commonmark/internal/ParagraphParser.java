package org.commonmark.internal;

import org.commonmark.node.*;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

import java.util.List;
import java.util.Map;

public class ParagraphParser extends AbstractBlockParser {

    private final Paragraph block = new Paragraph();
    private final LinkReferenceDefinitionParser linkReferenceDefinitionParser = new LinkReferenceDefinitionParser();

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
    public void addLine(SourceLine line) {
        linkReferenceDefinitionParser.parse(line);
    }

    @Override
    public void addSourceSpan(SourceSpan sourceSpan) {
        // Some source spans might belong to link reference definitions, others to the paragraph.
        // The parser will handle that.
        linkReferenceDefinitionParser.addSourceSpan(sourceSpan);
    }

    @Override
    public List<DefinitionMap<?>> getDefinitions() {
        var map = new DefinitionMap<>(LinkReferenceDefinition.class);
        for (var def : linkReferenceDefinitionParser.getDefinitions()) {
            map.putIfAbsent(def.getLabel(), def);
        }
        return List.of(map);
    }

    @Override
    public void closeBlock() {
        for (var def : linkReferenceDefinitionParser.getDefinitions()) {
            block.insertBefore(def);
        }

        if (linkReferenceDefinitionParser.getParagraphLines().isEmpty()) {
            block.unlink();
        } else {
            block.setSourceSpans(linkReferenceDefinitionParser.getParagraphSourceSpans());
        }
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        SourceLines lines = linkReferenceDefinitionParser.getParagraphLines();
        if (!lines.isEmpty()) {
            inlineParser.parse(lines, block);
        }
    }

    public SourceLines getParagraphLines() {
        return linkReferenceDefinitionParser.getParagraphLines();
    }
}
