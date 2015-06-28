package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.SourcePosition;
import org.commonmark.parser.BlockContinue;
import org.commonmark.parser.BlockStart;

import java.util.regex.Pattern;

public class HtmlBlockParser extends AbstractBlockParser {

    private static String BLOCKTAGNAME = "(?:article|header|aside|hgroup|iframe|blockquote|hr|body|li|map|button|object|canvas|ol|caption|output|col|p|colgroup|pre|dd|progress|div|section|dl|table|td|dt|tbody|embed|textarea|fieldset|tfoot|figcaption|th|figure|thead|footer|footer|tr|form|ul|h1|h2|h3|h4|h5|h6|video|script|style)";

    private static String HTMLBLOCKOPEN = "<(?:" + BLOCKTAGNAME + "[\\s/>]" + "|" +
            "/" + BLOCKTAGNAME + "[\\s>]" + "|" + "[?!])";

    private static Pattern HTML_BLOCK_OPEN = Pattern.compile('^' + HTMLBLOCKOPEN, Pattern.CASE_INSENSITIVE);


    private final HtmlBlock block = new HtmlBlock();
    private BlockContent content = new BlockContent();

    public HtmlBlockParser(SourcePosition pos) {
        block.setSourcePosition(pos);
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (!state.isBlank()) {
            return BlockContinue.of(state.getIndex());
        } else {
            return BlockContinue.none();
        }
    }

    @Override
    public boolean acceptsLine() {
        return true;
    }

    @Override
    public void addLine(CharSequence line) {
        content.add(line);
    }

    @Override
    public void finalizeBlock(InlineParser inlineParser) {
        block.setLiteral(content.getString());
        content = null;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence line = state.getLine();
            if (HTML_BLOCK_OPEN.matcher(line.subSequence(nextNonSpace, line.length())).find()) {
                // spaces are part of block, so use offset
                return BlockStart.of(new HtmlBlockParser(pos(state, nextNonSpace)), state.getIndex());
            } else {
                return BlockStart.none();
            }
        }
    }
}
