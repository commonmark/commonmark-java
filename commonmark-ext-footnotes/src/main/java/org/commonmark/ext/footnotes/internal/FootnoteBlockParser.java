package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.node.Block;
import org.commonmark.node.DefinitionMap;
import org.commonmark.parser.block.*;
import org.commonmark.text.Characters;

import java.util.List;

/**
 * Parser for a single {@link FootnoteDefinition} block.
 */
public class FootnoteBlockParser extends AbstractBlockParser {

    private final FootnoteDefinition block;

    public FootnoteBlockParser(String label) {
        block = new FootnoteDefinition(label);
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(Block childBlock) {
        return true;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        if (parserState.getIndent() >= 4) {
            // It looks like content needs to be indented by 4 so that it's part of a footnote (instead of starting a new block).
            return BlockContinue.atColumn(4);
        } else {
            // We're not continuing to give other block parsers a chance to interrupt this definition.
            // But if no other block parser applied (including another FootnotesBlockParser), we will
            // accept the line via lazy continuation (same as a block quote).
            return BlockContinue.none();
        }
    }

    @Override
    public List<DefinitionMap<?>> getDefinitions() {
        var map = new DefinitionMap<>(FootnoteDefinition.class);
        map.putIfAbsent(block.getLabel(), block);
        return List.of(map);
    }

    public static class Factory implements BlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }
            var index = state.getNextNonSpaceIndex();
            var content = state.getLine().getContent();
            if (content.charAt(index) != '[' || index + 1 >= content.length()) {
                return BlockStart.none();
            }
            index++;
            if (content.charAt(index) != '^' || index + 1 >= content.length()) {
                return BlockStart.none();
            }
            // Now at first label character (if any)
            index++;
            var labelStart = index;

            for (index = labelStart; index < content.length(); index++) {
                var c = content.charAt(index);
                switch (c) {
                    case ']':
                        if (index > labelStart && index + 1 < content.length() && content.charAt(index + 1) == ':') {
                            var label = content.subSequence(labelStart, index).toString();
                            // After the colon, any number of spaces is skipped (not part of the content)
                            var afterSpaces = Characters.skipSpaceTab(content, index + 2, content.length());
                            return BlockStart.of(new FootnoteBlockParser(label)).atIndex(afterSpaces);
                        } else {
                            return BlockStart.none();
                        }
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\0':
                    case '\t':
                        return BlockStart.none();
                }
            }

            return BlockStart.none();
        }
    }
}
