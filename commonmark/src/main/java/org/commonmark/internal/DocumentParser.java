package org.commonmark.internal;

import org.commonmark.internal.util.Parsing;
import org.commonmark.internal.util.Substring;
import org.commonmark.node.*;

import java.util.*;

public class DocumentParser {

    /**
     * 1-based line number
     */
    private int lineNumber = 0;
    private int lastLineLength = 0;
    private InlineParser inlineParser = new InlineParser();

    private List<BlockParserFactory> blockParserFactories = Arrays.<BlockParserFactory>asList(
            new IndentedCodeBlockParser.Factory(),
            new BlockQuoteParser.Factory(),
            new HeaderParser.Factory(),
            new FencedCodeBlockParser.Factory(),
            new HtmlBlockParser.Factory(),
            new HorizontalRuleParser.Factory(),
            new ListBlockParser.Factory());
    private List<BlockParser> activeBlockParsers = new ArrayList<>();
    private Set<BlockParser> allBlockParsers = new HashSet<>();
    private Map<Node, Boolean> lastLineBlank = new HashMap<>();

    public DocumentParser() {
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

    /**
     * Analyze a line of text and update the document appropriately. We parse markdown text by calling this on each
     * line of input, then finalizing the document.
     */
    private void incorporateLine(CharSequence ln) {
        int offset = 0;
        int nextNonSpace = 0;
        boolean blank = false;

        this.lineNumber += 1;

        ln = Parsing.prepareLine(ln);

        // For each containing block, try to parse the associated line start.
        // Bail out on failure: container will point to the last matching block.
        // Set all_matched to false if not all containers match.
        // The document will always match, can be skipped
        int matches = 1;
        for (BlockParser blockParser : activeBlockParsers.subList(1, activeBlockParsers.size())) {
            int match = Parsing.findNonSpace(ln, offset);
            if (match == -1) {
                nextNonSpace = ln.length();
                blank = true;
            } else {
                nextNonSpace = match;
                blank = false;
            }

            BlockParser.ContinueResult result = blockParser.continueBlock(ln, nextNonSpace, offset, blank);
            if (result instanceof BlockParser.BlockMatched) {
                BlockParser.BlockMatched blockMatched = (BlockParser.BlockMatched) result;
                offset = blockMatched.getNewOffset();
                matches++;
            } else if (result instanceof BlockParser.BlockMatchedAndCanBeFinalized) {
                finalize(blockParser, this.lineNumber);
                lastLineLength = ln.length() - 1; // -1 for newline
                return;
            } else if (result instanceof BlockParser.BlockDidNotMatch) {
                break;
            }
        }

        // TODO: Can we remove the closeUnmatchedBlocks calls?
        List<BlockParser> unmatchedBlockParsers = new ArrayList<>(activeBlockParsers.subList(matches, activeBlockParsers.size()));
        BlockParser blockParser = activeBlockParsers.get(matches - 1);
        boolean allClosed = unmatchedBlockParsers.isEmpty();

        // Check to see if we've hit 2nd blank line; if so break out of list:
        if (blank && isLastLineBlank(blockParser.getBlock())) {
            List<BlockParser> matchedBlockParsers = new ArrayList<>(activeBlockParsers.subList(0, matches));
            breakOutOfLists(matchedBlockParsers);
        }

        // Unless last matched container is a code block, try new container starts,
        // adding children to the last matched container:
        boolean blockStartsDone = false;
        while (!blockStartsDone) {
            int match = Parsing.findNonSpace(ln, offset);
            if (match == -1) {
                nextNonSpace = ln.length();
                blank = true;
                break;
            }
            nextNonSpace = match;
            blank = false;
            int indent = nextNonSpace - offset;

            if (!blockParser.shouldTryBlockStarts()) {
                break;
            }

            if (indent >= IndentedCodeBlockParser.INDENT && getActiveBlockParser().getBlock() instanceof Paragraph) {
                // An indented code block cannot interrupt a paragraph.
                offset = nextNonSpace;
                break;
            }

            // this is a little performance optimization:
            if (indent < IndentedCodeBlockParser.INDENT && Parsing.isLetter(ln, nextNonSpace)) {
                break;
            }

            blockStartsDone = true;
            for (BlockParserFactory blockParserFactory : blockParserFactories) {
                ParserStateImpl state = new ParserStateImpl(ln, offset, nextNonSpace, blockParser, lineNumber);
                BlockParserFactory.StartResult result = blockParserFactory.tryStart(state);
                if (result instanceof BlockParserFactory.BlockStart) {
                    BlockParserFactory.BlockStart blockStart = (BlockParserFactory.BlockStart) result;
                    allClosed = allClosed || finalizeBlocks(unmatchedBlockParsers);
                    offset = blockStart.getNewOffset();

                    if (blockStart.replaceActiveBlockParser()) {
                        removeActiveBlockParser();
                    }

                    for (BlockParser newBlockParser : blockStart.getBlockParsers()) {
                        blockParser = addChild(newBlockParser);
                        if (newBlockParser.shouldTryBlockStarts()) {
                            blockStartsDone = false;
                        }
                    }

                    break;
                }
            }
        }

        // What remains at the offset is a text line. Add the text to the
        // appropriate container.

        // First check for a lazy paragraph continuation:
        if (!allClosed && !blank &&
                getActiveBlockParser() instanceof ParagraphParser &&
                ((ParagraphParser) getActiveBlockParser()).hasLines()) {
            // lazy paragraph continuation
            addLine(ln, offset);

        } else { // not a lazy continuation

            // finalize any blocks not matched
            if (!allClosed) {
                finalizeBlocks(unmatchedBlockParsers);
            }
            propagateLastLineBlank(blockParser, blank);

            if (blockParser.acceptsLine()) {
                addLine(ln, offset);
            } else if (offset < ln.length() && !blank) {
                // create paragraph container for line
                addChild(new ParagraphParser(new SourcePosition(this.lineNumber, nextNonSpace + 1)));
                addLine(ln, nextNonSpace);
            }
        }
        this.lastLineLength = ln.length() - 1; // -1 for newline
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
    private void addLine(CharSequence ln, int offset) {
        getActiveBlockParser().addLine(ln.subSequence(offset, ln.length()));
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

    private BlockParser getActiveBlockParser() {
        return activeBlockParsers.get(activeBlockParsers.size() - 1);
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

    private static class ParserStateImpl implements BlockParserFactory.ParserState {

        private final CharSequence line;
        private final int offset;
        private final int nextNonSpace;
        private final BlockParser activeBlockParser;
        private final int lineNumber;

        public ParserStateImpl(CharSequence line, int offset, int nextNonSpace, BlockParser activeBlockParser, int lineNumber) {
            this.line = line;
            this.offset = offset;
            this.nextNonSpace = nextNonSpace;
            this.activeBlockParser = activeBlockParser;
            this.lineNumber = lineNumber;
        }

        @Override
        public CharSequence getLine() {
            return line;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getNextNonSpace() {
            return nextNonSpace;
        }

        @Override
        public BlockParser getActiveBlockParser() {
            return activeBlockParser;
        }

        @Override
        public int getLineNumber() {
            return lineNumber;
        }
    }
}
