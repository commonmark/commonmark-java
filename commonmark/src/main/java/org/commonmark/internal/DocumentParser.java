package org.commonmark.internal;

import org.commonmark.parser.BlockContinue;
import org.commonmark.parser.BlockStart;
import org.commonmark.parser.DelimiterProcessor;
import org.commonmark.internal.util.Parsing;
import org.commonmark.internal.util.Substring;
import org.commonmark.node.*;

import java.util.*;

public class DocumentParser implements ParserState {

    private static List<BlockParserFactory> CORE_FACTORIES = Arrays.<BlockParserFactory>asList(
            new IndentedCodeBlockParser.Factory(),
            new BlockQuoteParser.Factory(),
            new HeaderParser.Factory(),
            new FencedCodeBlockParser.Factory(),
            new HtmlBlockParser.Factory(),
            new HorizontalRuleParser.Factory(),
            new ListBlockParser.Factory());

    private CharSequence line;
    /**
     * 1-based line number
     */
    private int lineNumber = 0;
    private int index = 0;
    private int nextNonSpace = 0;

    private int lastLineLength = 0;

    private final InlineParser inlineParser;
    private final List<BlockParserFactory> blockParserFactories;

    private List<BlockParser> activeBlockParsers = new ArrayList<>();
    private Set<BlockParser> allBlockParsers = new HashSet<>();
    private Map<Node, Boolean> lastLineBlank = new HashMap<>();

    public DocumentParser(List<BlockParserFactory> customBlockParserFactories, List<DelimiterProcessor> delimiterProcessors) {
        blockParserFactories = new ArrayList<>(CORE_FACTORIES);
        blockParserFactories.addAll(customBlockParserFactories);
        inlineParser = new InlineParser(delimiterProcessors);
    }

    /**
     * The main parsing function. Returns a parsed document AST.
     */
    public Document parse(String input) {
        DocumentBlockParser documentBlockParser = new DocumentBlockParser();
        documentBlockParser.getBlock().setSourcePosition(new SourcePosition(1, 1));
        activateBlockParser(documentBlockParser);

        int lineStart = 0;
        int lineBreak;
        while ((lineBreak = Parsing.findLineBreak(input, lineStart)) != -1) {
            CharSequence line = Substring.of(input, lineStart, lineBreak);
            incorporateLine(line);
            if (lineBreak + 1 < input.length() && input.charAt(lineBreak) == '\r' && input.charAt(lineBreak + 1) == '\n') {
                lineStart = lineBreak + 2;
            } else {
                lineStart = lineBreak + 1;
            }
        }
        if (lineStart == 0 || lineStart < input.length()) {
            incorporateLine(Substring.of(input, lineStart, input.length()));
        }

        finalizeBlocks(activeBlockParsers, lineNumber);
        this.processInlines();
        return documentBlockParser.getBlock();
    }

    @Override
    public CharSequence getLine() {
        return line;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getNextNonSpaceIndex() {
        return nextNonSpace;
    }

    @Override
    public boolean isBlank() {
        return nextNonSpace == line.length();
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public BlockParser getActiveBlockParser() {
        return activeBlockParsers.get(activeBlockParsers.size() - 1);
    }

    private void findNextNonSpace() {
        int match = Parsing.findNonSpace(line, index);
        if (match == -1) {
            nextNonSpace = line.length();
        } else {
            nextNonSpace = match;
        }
    }

    /**
     * Analyze a line of text and update the document appropriately. We parse markdown text by calling this on each
     * line of input, then finalizing the document.
     */
    private void incorporateLine(CharSequence ln) {
        line = Parsing.prepareLine(ln);
        index = 0;
        nextNonSpace = 0;
        lineNumber += 1;

        // For each containing block, try to parse the associated line start.
        // Bail out on failure: container will point to the last matching block.
        // Set all_matched to false if not all containers match.
        // The document will always match, can be skipped
        int matches = 1;
        for (BlockParser blockParser : activeBlockParsers.subList(1, activeBlockParsers.size())) {
            findNextNonSpace();

            BlockContinue result = blockParser.tryContinue(this);
            if (result instanceof BlockContinueImpl) {
                BlockContinueImpl blockContinue = (BlockContinueImpl) result;
                if (blockContinue.isFinalize()) {
                    finalize(blockParser, this.lineNumber);
                    lastLineLength = line.length() - 1; // -1 for newline
                    return;
                } else {
                    index = blockContinue.getNewIndex();
                    matches++;
                }
            } else {
                break;
            }
        }

        // TODO: Can we remove the closeUnmatchedBlocks calls?
        List<BlockParser> unmatchedBlockParsers = new ArrayList<>(activeBlockParsers.subList(matches, activeBlockParsers.size()));
        BlockParser blockParser = activeBlockParsers.get(matches - 1);
        boolean allClosed = unmatchedBlockParsers.isEmpty();

        // Check to see if we've hit 2nd blank line; if so break out of list:
        if (isBlank() && isLastLineBlank(blockParser.getBlock())) {
            List<BlockParser> matchedBlockParsers = new ArrayList<>(activeBlockParsers.subList(0, matches));
            breakOutOfLists(matchedBlockParsers);
        }

        // Unless last matched container is a code block, try new container starts,
        // adding children to the last matched container:
        boolean tryBlockStarts = blockParser.getBlock() instanceof Paragraph || blockParser.isContainer();
        while (tryBlockStarts) {
            findNextNonSpace();
            int indent = getNextNonSpaceIndex() - getIndex();
            boolean codeIndent = indent >= IndentedCodeBlockParser.INDENT;

            // this is a little performance optimization:
            if (!codeIndent && Parsing.isLetter(line, nextNonSpace)) {
                index = nextNonSpace;
                break;
            }

            BlockStartImpl blockStart = findBlockStart(blockParser);
            if (blockStart == null) {
                index = nextNonSpace;
                break;
            }

            if (!allClosed) {
                finalizeBlocks(unmatchedBlockParsers);
                allClosed = true;
            }
            index = blockStart.getNewIndex();

            if (blockStart.replaceActiveBlockParser()) {
                removeActiveBlockParser();
            }

            for (BlockParser newBlockParser : blockStart.getBlockParsers()) {
                blockParser = addChild(newBlockParser);
                tryBlockStarts = newBlockParser.isContainer();
            }
        }

        // What remains at the offset is a text line. Add the text to the
        // appropriate block.

        // First check for a lazy paragraph continuation:
        if (!allClosed && !isBlank() &&
                getActiveBlockParser() instanceof ParagraphParser) {
            // lazy paragraph continuation
            addLine(index);

        } else {

            // finalize any blocks not matched
            if (!allClosed) {
                finalizeBlocks(unmatchedBlockParsers);
            }
            propagateLastLineBlank(blockParser, isBlank());

            if (!blockParser.isContainer()) {
                addLine(index);
            } else if (!isBlank()) {
                // create paragraph container for line
                addChild(new ParagraphParser(new SourcePosition(this.lineNumber, nextNonSpace + 1)));
                addLine(nextNonSpace);
            }
        }
        this.lastLineLength = ln.length() - 1; // -1 for newline
    }

    private BlockStartImpl findBlockStart(BlockParser blockParser) {
        MatchedBlockParser matchedBlockParser = new MatchedBlockParserImpl(blockParser);
        for (BlockParserFactory blockParserFactory : blockParserFactories) {
            BlockStart result = blockParserFactory.tryStart(this, matchedBlockParser);
            if (result instanceof BlockStartImpl) {
                return (BlockStartImpl) result;
            }
        }
        return null;
    }

    /**
     * Finalize a block. Close it and do any necessary postprocessing, e.g. creating string_content from strings,
     * setting the 'tight' or 'loose' status of a list, and parsing the beginnings of paragraphs for reference
     * definitions.
     */
    private void finalize(BlockParser blockParser, int lineNumber) {
        if (getActiveBlockParser() == blockParser) {
            deactivateBlockParser();
        }

        // TODO: Maybe this should be done in the block parser instead?
        Block block = blockParser.getBlock();
        SourcePosition pos = block.getSourcePosition();
        block.setSourcePosition(new SourcePosition(pos.getStartLine(), pos.getStartColumn(),
                lineNumber, this.lastLineLength + 1));

        blockParser.finalizeBlock(inlineParser);

        if (blockParser instanceof ListBlockParser) {
            ListBlockParser listBlockParser = (ListBlockParser) blockParser;
            finalizeListTight(listBlockParser);
        }
    }

    /**
     * Walk through a block & children recursively, parsing string content into inline content where appropriate.
     */
    private void processInlines() {
        for (BlockParser blockParser : allBlockParsers) {
            blockParser.processInlines(inlineParser);
        }
    }

    private void finalizeListTight(ListBlockParser listBlockParser) {
        Node item = listBlockParser.getBlock().getFirstChild();
        while (item != null) {
            // check for non-final list item ending with blank line:
            if (endsWithBlankLine(item) && item.getNext() != null) {
                listBlockParser.setTight(false);
                break;
            }
            // recurse into children of list item, to see if there are
            // spaces between any of them:
            Node subItem = item.getFirstChild();
            while (subItem != null) {
                if (endsWithBlankLine(subItem) && (item.getNext() != null || subItem.getNext() != null)) {
                    listBlockParser.setTight(false);
                    break;
                }
                subItem = subItem.getNext();
            }
            item = item.getNext();
        }
    }

    private boolean endsWithBlankLine(Node block) {
        while (block != null) {
            if (isLastLineBlank(block)) {
                return true;
            }
            if (block instanceof ListBlock || block instanceof ListItem) {
                block = block.getLastChild();
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * Break out of all containing lists, resetting the tip of the document to the parent of the highest list,
     * and finalizing all the lists. (This is used to implement the "two blank lines break of of all lists" feature.)
     */
    private void breakOutOfLists(List<BlockParser> blockParsers) {
        int lastList = -1;
        for (int i = blockParsers.size() - 1; i >= 0; i--) {
            BlockParser blockParser = blockParsers.get(i);
            if (blockParser instanceof ListBlockParser) {
                lastList = i;
            }
        }

        if (lastList != -1) {
            finalizeBlocks(blockParsers.subList(lastList, blockParsers.size()));
        }
    }

    /**
     * Add a line to the block at the tip. We assume the tip can accept lines -- that check should be done before
     * calling this.
     */
    private void addLine(int index) {
        getActiveBlockParser().addLine(line.subSequence(index, line.length()));
    }

    /**
     * Add block of type tag as a child of the tip. If the tip can't  accept children, close and finalize it and try
     * its parent, and so on til we find a block that can accept children.
     */
    private <T extends BlockParser> T addChild(T blockParser) {
        while (!getActiveBlockParser().canContain(blockParser.getBlock())) {
            this.finalize(getActiveBlockParser(), this.lineNumber - 1);
        }

        getActiveBlockParser().getBlock().appendChild(blockParser.getBlock());
        activateBlockParser(blockParser);

        return blockParser;
    }

    private void activateBlockParser(BlockParser blockParser) {
        activeBlockParsers.add(blockParser);
        allBlockParsers.add(blockParser);
    }

    private void deactivateBlockParser() {
        activeBlockParsers.remove(activeBlockParsers.size() - 1);
    }

    private void removeActiveBlockParser() {
        BlockParser old = getActiveBlockParser();
        deactivateBlockParser();
        allBlockParsers.remove(old);

        old.getBlock().unlink();
    }

    private void propagateLastLineBlank(BlockParser blockParser, boolean blank) {
        if (blank && blockParser.getBlock().getLastChild() != null) {
            setLastLineBlank(blockParser.getBlock().getLastChild(), true);
        }

        Block block = blockParser.getBlock();

        // Block quote lines are never blank as they start with >
        // and we don't count blanks in fenced code for purposes of tight/loose
        // lists or breaking out of lists. We also don't set last_line_blank
        // on an empty list item, or if we just closed a fenced block.
        boolean lastLineBlank = blank &&
                !(block instanceof BlockQuote || block instanceof FencedCodeBlock ||
                        (block instanceof ListItem &&
                                block.getFirstChild() == null &&
                                block.getSourcePosition().getStartLine() == this.lineNumber));

        // propagate lastLineBlank up through parents:
        Node node = blockParser.getBlock();
        while (node != null) {
            setLastLineBlank(node, lastLineBlank);
            node = node.getParent();
        }
    }

    private void setLastLineBlank(Node node, boolean value) {
        lastLineBlank.put(node, value);
    }

    private boolean isLastLineBlank(Node node) {
        Boolean value = lastLineBlank.get(node);
        return value != null && value;
    }

    /**
     * Finalize blocks of previous line. Returns true.
     */
    private boolean finalizeBlocks(List<BlockParser> blockParsers) {
        finalizeBlocks(blockParsers, lineNumber - 1);
        return true;
    }

    private void finalizeBlocks(List<BlockParser> blockParsers, int lineNumber) {
        for (int i = blockParsers.size() - 1; i >= 0; i--) {
            BlockParser blockParser = blockParsers.get(i);
            finalize(blockParser, lineNumber);
        }
    }

    private static class MatchedBlockParserImpl implements MatchedBlockParser {

        private final BlockParser matchedBlockParser;

        public MatchedBlockParserImpl(BlockParser matchedBlockParser) {
            this.matchedBlockParser = matchedBlockParser;
        }

        @Override
        public BlockParser getMatchedBlockParser() {
            return matchedBlockParser;
        }

        @Override
        public CharSequence getParagraphStartLine() {
            if (matchedBlockParser instanceof ParagraphParser) {
                ParagraphParser paragraphParser = (ParagraphParser) matchedBlockParser;
                if (paragraphParser.hasSingleLine()) {
                    return paragraphParser.getContentString();
                }
            }
            return null;
        }
    }
}
