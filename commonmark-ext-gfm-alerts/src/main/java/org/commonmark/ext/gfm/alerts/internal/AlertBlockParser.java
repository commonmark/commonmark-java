package org.commonmark.ext.gfm.alerts.internal;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.ext.gfm.alerts.Alert;
import org.commonmark.ext.gfm.alerts.AlertTitle;
import org.commonmark.node.Block;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.Document;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;
import org.commonmark.text.Characters;

public class AlertBlockParser extends AbstractBlockParser {

    private static final Pattern ALERT_PATTERN_NO_CUSTOM_TITLE = Pattern.compile("^\\[!([a-zA-Z]+)]\\s*$");
    private static final Pattern ALERT_PATTERN_CUSTOM_TITLE = Pattern.compile("^\\[!([a-zA-Z]+)](.*)$");

    private final Alert block;
    private final String typeOriginalCase;
    private final String titleContent;

    private AlertBlockParser(String type, String typeOriginalCase, String titleContent) {
        this.block = new Alert(type);
        this.typeOriginalCase = typeOriginalCase;
        this.titleContent = titleContent;
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
    public BlockContinue tryContinue(ParserState state) {
        /*
         * Same continuation rule as a block quote: line must start with '>'
         * (with up to 3 leading spaces, optional space after '>')
         */
        var line = state.getLine().getContent();
        int nextNonSpace = state.getNextNonSpaceIndex();
        if (state.getIndent() >= 4 // Parsing.CODE_BLOCK_INDENT
                || nextNonSpace >= line.length()
                || line.charAt(nextNonSpace) != '>') {
            return BlockContinue.none();
        }

        int newColumn = state.getColumn() + state.getIndent() + 1;
        if (Characters.isSpaceOrTab(line, nextNonSpace + 1)) {
            newColumn++;
        }

        return BlockContinue.atColumn(newColumn);
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        // Determine if there is any non-title body content.
        if (block.getFirstChild() == null) {
            /*
             * Replace the Alert with a BlockQuote whose only paragraph contains
             * the original first line text.
             */
            demoteToBlockQuote(inlineParser);
            return;
        }

        if (titleContent.isEmpty()) {
            return;
        }

        /*
         * Inline-parse the title in its own scope so delimiters are isolated
         * from the body text. For example:
         *
         * > [!NOTE] 2*2 = 4
         * > But 3*3 = 9
         */
        var titleNode = new AlertTitle();
        inlineParser.parse(SourceLines.of(SourceLine.of(titleContent, null)), titleNode);

        // Body blocks were attached as children during block parsing. Prepend the title.
        block.prependChild(titleNode);
    }

    private void demoteToBlockQuote(InlineParser inlineParser) {
        var bq = new BlockQuote();
        bq.setSourceSpans(block.getSourceSpans());
        var p = new Paragraph();

        // Build the literal text including the alert marker and title.
        var literal = "[!" + typeOriginalCase + "]";
        if (!titleContent.isEmpty()) {
            /*
             * This may not preserve the original number of spaces between the
             * alert marker and title (e.g., if there were 0 or 2+ spaces).
             */
            literal += " " + titleContent;
        }

        // Parse the inlines of the full content (alert marker + title)
        inlineParser.parse(SourceLines.of(SourceLine.of(literal, null)), p);
        bq.appendChild(p);
        block.insertAfter(bq);
        block.unlink();
    }

    public static class Factory extends AbstractBlockParserFactory {

        private final Set<String> allowedTypes;
        private final boolean customTitlesAllowed;
        private final boolean nestedAlertsAllowed;

        public Factory(Set<String> allowedTypes, boolean customTitlesAllowed, boolean nestedAlertsAllowed) {
            this.allowedTypes = allowedTypes;
            this.customTitlesAllowed = customTitlesAllowed;
            this.nestedAlertsAllowed = nestedAlertsAllowed;
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            // Parsing.CODE_BLOCK_INDENT
            if (state.getIndent() >= 4) {
                return BlockStart.none();
            }

            if (!nestedAlertsAllowed && !isAtRoot(matchedBlockParser.getMatchedBlockParser().getBlock())) {
                return BlockStart.none();
            }

            var line = state.getLine().getContent();
            int nextNonSpace = state.getNextNonSpaceIndex();

            // Case A: Fresh start. Line begins with '>'.
            if (nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
                return tryStartFresh(line, nextNonSpace, state);
            }

            /*
             * Case B: Promotion. We're already inside a BlockQuote whose body so far is
             * empty (only blank '>' lines), and the current line (already stripped of
             * its '>' prefix by BlockQuoteParser.tryContinue) is the marker. Replace the
             * active block quote with an alert.
             */
            var matched = matchedBlockParser.getMatchedBlockParser().getBlock();
            if (matched instanceof BlockQuote && matched.getFirstChild() == null) {
                // Null if not a marker. null.replaceActiveBlockParser() would NPE, so guard.
                return tryStartFresh(line, nextNonSpace, state);
            }

            return BlockStart.none();
        }

        private static boolean isAtRoot(Block matched) {
            if (matched instanceof Document) {
                return true;
            }

            /*
             * Case B: Promotion. The matched block is a top-level (Document-parented)
             * BlockQuote that's still empty.
             */
            if (matched instanceof BlockQuote
                    && matched.getFirstChild() == null
                    && matched.getParent() instanceof Document) {
                return true;
            }

            return false;
        }

        private BlockStart tryStartFresh(CharSequence line, int nextNonSpace, ParserState state) {
            int afterGt;
            if (nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
                afterGt = nextNonSpace + 1;
                if (Characters.isSpaceOrTab(line, afterGt)) {
                    afterGt++;
                }
            } else {
                /*
                 * Promotion path: the '>' has already been consumed by the active
                 * block quote's tryContinue, so state.getIndex() points past it.
                 */
                afterGt = state.getIndex();
            }

            Matcher matcher;
            if (customTitlesAllowed) {
                matcher = ALERT_PATTERN_CUSTOM_TITLE.matcher(line.subSequence(afterGt, line.length()));
            } else {
                matcher = ALERT_PATTERN_NO_CUSTOM_TITLE.matcher(line.subSequence(afterGt, line.length()));
            }

            if (!matcher.matches()) {
                return BlockStart.none();
            }

            String typeOriginalCase = matcher.group(1);
            String type = typeOriginalCase.toUpperCase(Locale.ROOT);
            if (!allowedTypes.contains(type)) {
                return BlockStart.none();
            }

            String titleContent = "";
            if (customTitlesAllowed) {
                titleContent = matcher.group(2).replaceFirst("^[ \\t]+", "").stripTrailing();
            }

            // Consume the rest of the first line.
            var start = BlockStart.of(new AlertBlockParser(type, typeOriginalCase, titleContent)).atIndex(line.length());

            // If we got here via the promotion path, replace the empty BlockQuote.
            var matched = state.getActiveBlockParser().getBlock();
            if (matched instanceof BlockQuote && matched.getFirstChild() == null
                    && (nextNonSpace >= line.length() || line.charAt(nextNonSpace) != '>')) {
                start = start.replaceActiveBlockParser();
            }

            return start;
        }
    }
}
