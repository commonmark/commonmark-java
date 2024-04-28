package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.node.Block;
import org.commonmark.parser.block.*;

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
    public boolean canHaveLazyContinuationLines() {
        return true;
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        // We're not continuing to give other block parsers a chance to interrupt this definition.
        // But if no other block parser applied (including another FootnotesBlockParser), we will
        // accept the line via lazy continuation.
        return BlockContinue.none();
    }

    public static class Factory implements BlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            var content = state.getLine().getContent();
            // TODO: Can it be indented? Maybe less than code block indent.
            var index = state.getNextNonSpaceIndex();
            if (content.charAt(index) != '[' || index + 1 >= content.length()) {
                return BlockStart.none();
            }
            index++;
            if (content.charAt(index) != '^' || index + 1 >= content.length()) {
                return BlockStart.none();
            }
            // Now at first label character (if any)
            index++;

            for (int i = index; i < content.length(); i++) {
                var c = content.charAt(i);
                if (c == ']') {
                    if (i > index) {
                        var label = content.subSequence(index, i).toString();
                        return BlockStart.of(new FootnoteBlockParser(label));
                    } else {
                        return BlockStart.none();
                    }
                }
                // TODO: Check what GitHub actually does here, e.g. tabs, control characters, other Unicode whitespace
                if (Character.isWhitespace(c)) {
                    return BlockStart.none();
                }
            }

            return BlockStart.none();
        }
    }
}
