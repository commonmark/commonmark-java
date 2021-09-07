package org.commonmark.internal;

import java.util.regex.Pattern;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Block;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

public class HtmlBlockParser extends AbstractBlockParser {

    private static final Pattern[][] BLOCK_PATTERNS = new Pattern[][]{
            {null, null}, // not used (no type 0)
            {
                    Pattern.compile("^<(?:script|pre|style|textarea)(?:\\s|>|$)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("</(?:script|pre|style|textarea)>", Pattern.CASE_INSENSITIVE)
            },
            {
                    Pattern.compile("^<!--"),
                    Pattern.compile("-->")
            },
            {
                    Pattern.compile("^<[?]"),
                    Pattern.compile("\\?>")
            },
            {
                    Pattern.compile("^<![A-Z]"),
                    Pattern.compile(">")
            },
            {
                    Pattern.compile("^<!\\[CDATA\\["),
                    Pattern.compile("\\]\\]>")
            },
            {
                    Pattern.compile("^</?(?:" +
                            "address|article|aside|" +
                            "base|basefont|blockquote|body|" +
                            "caption|center|col|colgroup|" +
                            "dd|details|dialog|dir|div|dl|dt|" +
                            "fieldset|figcaption|figure|footer|form|frame|frameset|" +
                            "h1|h2|h3|h4|h5|h6|head|header|hr|html|" +
                            "iframe|" +
                            "legend|li|link|" +
                            "main|menu|menuitem|" +
                            "nav|noframes|" +
                            "ol|optgroup|option|" +
                            "p|param|" +
                            "section|source|summary|" +
                            "table|tbody|td|tfoot|th|thead|title|tr|track|" +
                            "ul" +
                            ")(?:\\s|[/]?[>]|$)", Pattern.CASE_INSENSITIVE),
                    null // terminated by blank line
            },
            {
                    Pattern.compile("^(?:" + Parsing.OPENTAG + '|' + Parsing.CLOSETAG + ")\\s*$", Pattern.CASE_INSENSITIVE),
                    null // terminated by blank line
            }
    };

    private final HtmlBlock block = new HtmlBlock();
    private final Pattern closingPattern;

    private boolean finished = false;
    private BlockContent content = new BlockContent();
    private BlockContent rawContent = new BlockContent();

    private HtmlBlockParser(Pattern closingPattern) {
        this.closingPattern = closingPattern;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        if (finished) {
            return BlockContinue.none();
        }

        // Blank line ends type 6 and type 7 blocks
        if (state.isBlank() && closingPattern == null) {
            return BlockContinue.none();
        } else {
            return BlockContinue.atIndex(state.getIndex());
        }
    }

    @Override
    public void addLine(SourceLine line) {
        // Raw line and literal line are the same
        if(line.getLiteralIndex() == 0) {
            content.add(line.getContent());
            rawContent.add(line.getContent());
        }
        // Raw line and literal line are different
        else {
            content.add(line.getLiteralLine().getContent());
            
            // Use the literal line for anything which may be a block quote
            if(line != null && line.getContent().toString().contains(">")) {
                rawContent.add(line.getContent());
            }else {
                rawContent.add(line.getLiteralLine().getContent());
            }
        }

        if (closingPattern != null && closingPattern.matcher(line.getContent()).find()) {
            finished = true;
        }
    }

    @Override
    public void closeBlock() {
        block.setLiteral(content.getString());
        block.setRaw(rawContent.getString());
        content = null;
        rawContent = null;
    }

    public static class Factory extends AbstractBlockParserFactory {

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            CharSequence line = state.getLine().getContent();

            if (state.getIndent() < 4 && line.charAt(nextNonSpace) == '<') {
                for (int blockType = 1; blockType <= 7; blockType++) {
                    // Type 7 can not interrupt a paragraph (not even a lazy one)
                    if (blockType == 7 && (
                            matchedBlockParser.getMatchedBlockParser().getBlock() instanceof Paragraph ||
                                    state.getActiveBlockParser().canHaveLazyContinuationLines())) {
                        continue;
                    }
                    Pattern opener = BLOCK_PATTERNS[blockType][0];
                    Pattern closer = BLOCK_PATTERNS[blockType][1];
                    boolean matches = opener.matcher(line.subSequence(nextNonSpace, line.length())).find();
                    if (matches) {
                        return BlockStart.of(new HtmlBlockParser(closer)).atIndex(state.getIndex());
                    }
                }
            }
            return BlockStart.none();
        }
    }
}
