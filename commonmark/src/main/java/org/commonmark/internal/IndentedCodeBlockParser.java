package org.commonmark.internal;

import java.util.ArrayList;
import java.util.List;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

public class IndentedCodeBlockParser extends AbstractBlockParser {

    private final IndentedCodeBlock block = new IndentedCodeBlock();
    private final List<CharSequence> lines = new ArrayList<>();
    private final List<CharSequence> rawLines = new ArrayList<>();

    // Preserve original default constructor by explicitly defining one
    public IndentedCodeBlockParser() {
        super();
    }
    
    public IndentedCodeBlockParser(String indentWhitespace) {
        block.setIndentWhitespace(indentWhitespace);
    }
    
    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT) {
            return BlockContinue.atColumn(state.getColumn() + Parsing.CODE_BLOCK_INDENT);
        } else if (state.isBlank()) {
            return BlockContinue.atIndex(state.getNextNonSpaceIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public void addLine(SourceLine line) {
        String prefix = "";
        
        // Capture the prefix (needed for roundtrip, but unnecessary for HTML processing)
        if(line.getLiteralIndex() != 0) {
            prefix = line.substring(0, line.getLiteralIndex()).getContent().toString();
            line = line.getLiteralLine();
        }
        
        // Strip leading whitespace off first raw line (because it's already
        //    captured by the whitespace tracker)
        if(rawLines != null && rawLines.size() == 0) {
            rawLines.add(line.getContent().toString().replaceFirst("^\\s+", ""));
        }else {
            rawLines.add(prefix + line.getContent());
        }
        
        lines.add(line.getContent());
    }

    @Override
    public void closeBlock() {
        int lastNonBlank = lines.size() - 1;
        while (lastNonBlank >= 0) {
            if (!Parsing.isBlank(lines.get(lastNonBlank))) {
                break;
            }
            lastNonBlank--;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lastNonBlank + 1; i++) {
            sb.append(lines.get(i));
            sb.append('\n');
        }

        // The "literal" string is optimized for HTML formatting, but omits some details
        //    which are important for roundtrip rendering
        String literal = sb.toString();
        
        // Reuse the existing StringBuilder as a small optimization
        sb.setLength(0);
        for (int i = 0; i < rawLines.size(); i++) {
            sb.append(rawLines.get(i));
            
            // Don't append a newline if this block only contains one line, or to the
            //    last line if there is more than one line
            if(rawLines.size() != 1 && i != rawLines.size() - 1) {
                sb.append('\n');
            }
        }
        
        // The "raw" string is, as much as possible, the entire raw content as it was first
        //    entered into the parser. This allows roundtrip parsing for indented code blocks.
        String raw = sb.toString();
                
        block.setLiteral(literal);
        block.setRaw(raw);
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            // An indented code block cannot interrupt a paragraph.
            if (state.getIndent() >= Parsing.CODE_BLOCK_INDENT && !state.isBlank() && !(state.getActiveBlockParser().getBlock() instanceof Paragraph)) {
                String indentWhitespace = Parsing.collectWhitespace(state.getLine().getContent(), 0, state.getLine().getContent().length());
                
                return BlockStart.of(new IndentedCodeBlockParser(indentWhitespace)).atColumn(state.getColumn() + Parsing.CODE_BLOCK_INDENT);
            } else {
                return BlockStart.none();
            }
        }
    }
}

