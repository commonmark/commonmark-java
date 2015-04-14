package org.commonmark.internal;

import org.commonmark.node.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentParser {

	static int CODE_INDENT = 4;

	private static Pattern reMaybeSpecial = Pattern.compile("^[#`~*+_=<>0-9-]");

	private static Pattern reNonSpace = Pattern.compile("[^ \t\n]");

	private static Pattern reLineEnding = Pattern.compile("\r\n|\n|\r");

	private static final String[] tabSpaces = new String[] { "    ", "   ", "  ", " " };

	/** 1-based line number */
	private int lineNumber = 0;
	private int lastLineLength = 0;
	private InlineParser inlineParser = new InlineParser();

	private List<BlockParserFactory> blockParserFactories = Arrays.asList(
			new BlockQuoteParser.Factory(),
			new HeaderParser.Factory(),
			new CodeBlockParser.Factory(),
			new HtmlBlockParser.Factory(),
			new HorizontalRuleParser.Factory(),
			new ListBlockParser.Factory());
	private List<BlockParser> activeBlockParsers = new ArrayList<>();
	private Set<BlockParser> allBlockParsers = new HashSet<>();
	private Map<Node, Boolean> lastLineBlank = new HashMap<>();

	public DocumentParser() {
	}

	// The main parsing function. Returns a parsed document AST.
	public Document parse(String input) {
		DocumentBlockParser documentBlockParser = new DocumentBlockParser();
		documentBlockParser.getBlock().setSourcePosition(new SourcePosition(1, 1));
		activateBlockParser(documentBlockParser);

		// if (this.options.time) { console.time("preparing input"); }
		String[] lines = reLineEnding.split(input, -1);
		int len = lines.length;
		if (input.charAt(input.length() - 1) == '\n') {
			// ignore last blank line created by final newline
			len -= 1;
		}

		// if (this.options.time) { console.timeEnd("preparing input"); }
		// if (this.options.time) { console.time("block parsing"); }
		for (int i = 0; i < len; i++) {
			this.incorporateLine(lines[i]);
		}
		finalizeBlocks(activeBlockParsers, lineNumber);
		// if (this.options.time) { console.timeEnd("block parsing"); }
		// if (this.options.time) { console.time("inline parsing"); }
		this.processInlines();
		// if (this.options.time) { console.timeEnd("inline parsing"); }
		return documentBlockParser.getBlock();
	}

	// Walk through a block & children recursively, parsing string content
	// into inline content where appropriate. Returns new object.
	private void processInlines() {
		for (BlockParser blockParser : allBlockParsers) {
			blockParser.processInlines(inlineParser);
		}
	}

	// Analyze a line of text and update the document appropriately.
	// We parse markdown text by calling this on each line of input,
	// then finalizing the document.
	private void incorporateLine(String ln) {
		int offset = 0;
		int nextNonSpace = 0;
		boolean blank = false;

		this.lineNumber += 1;

		// replace NUL characters for security
		ln = ln.replace('\0', '\uFFFD');

		// Convert tabs to spaces:
		ln = detabLine(ln);

		// For each containing block, try to parse the associated line start.
		// Bail out on failure: container will point to the last matching block.
		// Set all_matched to false if not all containers match.
		// The document will always match, can be skipped
		int matches = 1;
		for (BlockParser blockParser : activeBlockParsers.subList(1, activeBlockParsers.size())) {
			int match = matchAt(reNonSpace, ln, offset);
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
			int match = matchAt(reNonSpace, ln, offset);
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

			if (indent >= CODE_INDENT) {
				// indented code or lazy paragraph continuation
				if (getActiveBlockParser().getBlock().getType() != Node.Type.Paragraph) {
					offset += CODE_INDENT;
					allClosed = allClosed || finalizeBlocks(unmatchedBlockParsers);
					blockParser = addChild(new CodeBlockParser(new SourcePosition(this.lineNumber, nextNonSpace)));
				}
				break;
			}

			// this is a little performance optimization:
			if (matchAt(reMaybeSpecial, ln, nextNonSpace) == -1) {
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

			// foo: on DocParser? Looks like an error to me
			// this.last_line_blank = false;
			this.addLine(ln, offset);

		} else { // not a lazy continuation

			// finalize any blocks not matched
			if (!allClosed) {
				finalizeBlocks(unmatchedBlockParsers);
			}
			propagateLastLineBlank(blockParser, blank);

			switch (blockParser.getBlock().getType()) {
				case HtmlBlock:
				case CodeBlock:
					this.addLine(ln, offset);
					break;

				case Header:
				case HorizontalRule:
					// nothing to do; we already added the contents.
					break;

				default:
					if (blockParser.acceptsLine()) {
						this.addLine(ln, nextNonSpace);
					} else if (!blank) {
						// create paragraph container for line
						// foo: in JS, there's a third argument, which looks like a bug
						addChild(new ParagraphParser(new SourcePosition(this.lineNumber, nextNonSpace + 1)));
						this.addLine(ln, nextNonSpace);
					}
			}
		}
		this.lastLineLength = ln.length() - 1; // -1 for newline
	}

	// Finalize a block. Close it and do any necessary postprocessing,
	// e.g. creating string_content from strings, setting the 'tight'
	// or 'loose' status of a list, and parsing the beginnings
	// of paragraphs for reference definitions. Reset the tip to the
	// parent of the closed block.
	private void finalize(BlockParser blockParser, int lineNumber) {
		// foo: top? looks like a bug
		// var above = block.parent || this.top;

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
			Node.Type t = block.getType();
			if (t == Node.Type.List || t == Node.Type.Item) {
				block = block.getLastChild();
			} else {
				break;
			}
		}
		return false;
	}

	// Attempt to match a regex in string s at offset offset.
	// Return index of match or -1.
	private static int matchAt(Pattern pattern, String string, int offset) {
		if (offset >= string.length()) {
			return -1;
		}
		Matcher matcher = pattern.matcher(string.substring(offset));
		boolean res = matcher.find();
		if (!res) {
			return -1;
		} else {
			return offset + matcher.start();
		}
	}

	// Break out of all containing lists, resetting the tip of the
	// document to the parent of the highest list, and finalizing
	// all the lists. (This is used to implement the "two blank lines
	// break of of all lists" feature.)
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

	// Add a line to the block at the tip. We assume the tip
	// can accept lines -- that check should be done before calling this.
	private void addLine(String ln, int offset) {
		getActiveBlockParser().addLine(ln.substring(offset));
	}

	// Add block of type tag as a child of the tip. If the tip can't
	// accept children, close and finalize it and try its parent,
	// and so on til we find a block that can accept children.
	private <T extends BlockParser> T addChild(T blockParser) {
		while (!getActiveBlockParser().canContain(blockParser.getBlock().getType())) {
			this.finalize(getActiveBlockParser(), this.lineNumber - 1);
		}

		getActiveBlockParser().getBlock().appendChild(blockParser.getBlock());
		activateBlockParser(blockParser);

		return blockParser;
	}

	private <T extends BlockParser> T replaceBlock(T blockParser) {
		BlockParser old = getActiveBlockParser();
		deactivateBlockParser();
		allBlockParsers.remove(old);

		old.getBlock().insertAfter(blockParser.getBlock());
		old.getBlock().unlink();
		blockParser.getBlock().setSourcePosition(old.getBlock().getSourcePosition());
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

	private SourcePosition getSourcePos(int offset) {
		int column_number = offset + 1; // offset 0 = column 1
		return new SourcePosition(this.lineNumber, column_number);
	}

	private void propagateLastLineBlank(BlockParser blockParser, boolean blank) {
		if (blank && blockParser.getBlock().getLastChild() != null) {
			setLastLineBlank(blockParser.getBlock().getLastChild(), true);
		}

		Block block = blockParser.getBlock();
		Node.Type t = block.getType();

		// Block quote lines are never blank as they start with >
		// and we don't count blanks in fenced code for purposes of tight/loose
		// lists or breaking out of lists. We also don't set last_line_blank
		// on an empty list item, or if we just closed a fenced block.
		boolean lastLineBlank = blank &&
				!(t == Node.Type.BlockQuote ||
						(t == Node.Type.CodeBlock && ((CodeBlock) block).isFenced()) ||
						(t == Node.Type.Item &&
								block.getFirstChild() == null &&
								block.getSourcePosition().getStartLine() == this.lineNumber));

		// propagate lastLineBlank up through parents:
		Node cont = blockParser.getBlock();
		while (cont != null) {
			setLastLineBlank(cont, lastLineBlank);
			cont = cont.getParent();
		}
	}

	private void setLastLineBlank(Node node, boolean value) {
		lastLineBlank.put(node, value);
	}

	private boolean isLastLineBlank(Node node) {
		Boolean value = lastLineBlank.get(node);
		return value != null && value;
	}

	// Finalize blocks of previous line. Returns true.
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

	// Returns true if string contains only space characters.
	static boolean isBlank(String s) {
		return !(reNonSpace.matcher(s).find());
	}

	// Convert tabs to spaces on each line using a 4-space tab stop.
	private static String detabLine(String text) {
		int start = 0;
		int offset;
		int lastStop = 0;

		while ((offset = text.indexOf("\t", start)) != -1) {
			int numspaces = (offset - lastStop) % 4;
			String spaces = tabSpaces[numspaces];
			text = text.substring(0, offset) + spaces + text.substring(offset + 1);
			lastStop = offset + numspaces;
			start = lastStop;
		}

		return text;
	}

	private static class ParserStateImpl implements BlockParserFactory.ParserState {

		private final String line;
		private final int offset;
		private final int nextNonSpace;
		private final BlockParser activeBlockParser;
		private final int lineNumber;

		public ParserStateImpl(String line, int offset, int nextNonSpace, BlockParser activeBlockParser, int lineNumber) {
			this.line = line;
			this.offset = offset;
			this.nextNonSpace = nextNonSpace;
			this.activeBlockParser = activeBlockParser;
			this.lineNumber = lineNumber;
		}

		@Override
		public String getLine() {
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
