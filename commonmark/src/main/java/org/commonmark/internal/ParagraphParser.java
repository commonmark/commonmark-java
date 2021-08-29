package org.commonmark.internal;

import java.util.List;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

public class ParagraphParser extends AbstractBlockParser {

    private final Paragraph block = new Paragraph();
    private final LinkReferenceDefinitionParser linkReferenceDefinitionParser = new LinkReferenceDefinitionParser();

    private String postContentWhitespace = "";
    private String postBlockWhitespace = "";
    
    // Preserve original default constructor by explicitly defining one
    public ParagraphParser() {
        super();
    }
    
    public ParagraphParser(String preBlockWhitespace) {
        block.setWhitespace(preBlockWhitespace);
        postContentWhitespace = "";
        postBlockWhitespace = "";
    }
    
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
    	char setextCheck = Character.MIN_VALUE;
        postBlockWhitespace = block.whitespacePostBlock();
        
        if (!state.isBlank()) {
            
            if(state.getLine() != null && state.getLine().getContent().toString().replaceFirst("^\\s+", "").length() != 0) {
                setextCheck = state.getLine().getContent().toString().replaceFirst("^\\s+", "").charAt(0);
            }
            
            if(setextCheck != Character.MIN_VALUE && setextCheck != '=' && setextCheck != '-') {
                // Collect post-content whitespace for roundtrip purposes
                postContentWhitespace = Parsing.collectWhitespaceBackwards(state.getLine().getContent(), state.getLine().getContent().length() - 1, 0);
            }
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
    public void closeBlock() {
        block.setWhitespace(block.whitespacePreBlock(), block.whitespacePreContent(), postContentWhitespace, postBlockWhitespace);
        
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
            inlineParser.parse(getRawParagraphLines(), block);
        }
    }

    public SourceLines getParagraphLines() {
        return linkReferenceDefinitionParser.getParagraphLines();
    }
    
    public SourceLines getRawParagraphLines() {
        return linkReferenceDefinitionParser.getRawParagraphLines();
    }
    
    public void setRawParagraphLines(SourceLines rawParagraphLines) {
        linkReferenceDefinitionParser.setRawParagraphLines(rawParagraphLines);
    }

    public List<LinkReferenceDefinition> getDefinitions() {
        return linkReferenceDefinitionParser.getDefinitions();
    }
}
