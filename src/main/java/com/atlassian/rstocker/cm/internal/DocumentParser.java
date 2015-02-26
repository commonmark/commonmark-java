package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentParser {

	static char C_GREATERTHAN = 62;
	static int CODE_INDENT = 4;

	private static char C_NEWLINE = 10;
	private static char C_SPACE = 32;
	private static char C_OPEN_BRACKET = 91;

	private static String BLOCKTAGNAME = "(?:article|header|aside|hgroup|iframe|blockquote|hr|body|li|map|button|object|canvas|ol|caption|output|col|p|colgroup|pre|dd|progress|div|section|dl|table|td|dt|tbody|embed|textarea|fieldset|tfoot|figcaption|th|figure|thead|footer|footer|tr|form|ul|h1|h2|h3|h4|h5|h6|video|script|style)";

	private static String HTMLBLOCKOPEN = "<(?:" + BLOCKTAGNAME + "[\\s/>]" + "|" +
			"/" + BLOCKTAGNAME + "[\\s>]" + "|" + "[?!])";

	private static Pattern reHtmlBlockOpen = Pattern.compile('^' + HTMLBLOCKOPEN, Pattern.CASE_INSENSITIVE);

	private static Pattern reHrule = Pattern
			.compile("^(?:(?:\\* *){3,}|(?:_ *){3,}|(?:- *){3,}) *$");

	private static Pattern reMaybeSpecial = Pattern.compile("^[#`~*+_=<>0-9-]");

	private static Pattern reNonSpace = Pattern.compile("[^ \t\n]");

	private static Pattern reBulletListMarker = Pattern.compile("^[*+-]( +|$)");

	private static Pattern reOrderedListMarker = Pattern.compile("^(\\d+)([.)])( +|$)");

	private static Pattern reATXHeaderMarker = Pattern.compile("^#{1,6}(?: +|$)");

	private static Pattern reCodeFence = Pattern.compile("^`{3,}(?!.*`)|^~{3,}(?!.*~)");

	private static Pattern reClosingCodeFence = Pattern.compile("^(?:`{3,}|~{3,})(?= *$)");

	private static Pattern reSetextHeaderLine = Pattern.compile("^(?:=+|-+) *$");

	private static Pattern reLineEnding = Pattern.compile("\r\n|\n|\r");

	private int lineNumber = 0;
	private int lastLineLength = 0;
	private InlineParser inlineParser = new InlineParser();

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
		if (input.charAt(input.length() - 1) == C_NEWLINE) {
			// ignore last blank line created by final newline
			len -= 1;
		}

		// if (this.options.time) { console.timeEnd("preparing input"); }
		// if (this.options.time) { console.time("block parsing"); }
		for (int i = 0; i < len; i++) {
			this.incorporateLine(lines[i]);
		}
		for (int i = activeBlockParsers.size() - 1; i >= 0; i--) {
			finalize(activeBlockParsers.get(i), len);
		}
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
		int nextNonSpace;
		int offset = 0;
		int match;
		ListData data;
		boolean blank = false;
		int indent = 0;
		int CODE_INDENT = 4;

		this.lineNumber += 1;

		// replace NUL characters for security
		if (ln.indexOf("\u0000") != -1) {
			ln = ln.replace("\0", "\uFFFD");
		}

		// Convert tabs to spaces:
		ln = detabLine(ln);


		// For each containing block, try to parse the associated line start.
		// Bail out on failure: container will point to the last matching block.
		// Set all_matched to false if not all containers match.
		// The document will always match, can be skipped
		int matches = 1;
		for (BlockParser blockParser : activeBlockParsers.subList(1, activeBlockParsers.size())) {
			match = matchAt(reNonSpace, ln, offset);
			if (match == -1) {
				nextNonSpace = ln.length();
				blank = true;
			} else {
				nextNonSpace = match;
				blank = false;
			}

			// TODO: use proper result object probably
			int[] offsetBox = {offset};
			BlockParser.ContinueResult result = blockParser.continueBlock(ln, nextNonSpace, offsetBox, blank);
			offset = offsetBox[0];

			if (result == BlockParser.ContinueResult.MATCHED) {
				matches++;
			} else if (result == BlockParser.ContinueResult.NOT_MATCHED) {
				break;
			} else if (result == BlockParser.ContinueResult.FINALIZE) {
				finalize(blockParser, this.lineNumber);
				lastLineLength = ln.length() - 1; // -1 for newline
				return;
			}
		}

		// TODO: Can we remove the closeUnmatchedBlocks calls?
		List<BlockParser> unmatchedBlockParsers = new ArrayList<>(activeBlockParsers.subList(matches, activeBlockParsers.size()));
		BlockParser container = activeBlockParsers.get(matches - 1);
		boolean allClosed = unmatchedBlockParsers.isEmpty();

		// Check to see if we've hit 2nd blank line; if so break out of list:
		if (blank && isLastLineBlank(container.getBlock())) {
			List<BlockParser> matchedBlockParsers = new ArrayList<>(activeBlockParsers.subList(0, matches));
			breakOutOfLists(matchedBlockParsers);
		}

		// Unless last matched container is a code block, try new container starts,
		// adding children to the last matched container:
		while (true) {
			Node.Type t = container.getBlock().getType();

			match = matchAt(reNonSpace, ln, offset);
			if (match == -1) {
				nextNonSpace = ln.length();
				blank = true;
				break;
			} else {
				nextNonSpace = match;
				blank = false;
			}
			indent = nextNonSpace - offset;

			if (t == Node.Type.CodeBlock || t == Node.Type.HtmlBlock) {
				break;
			}

			if (indent >= CODE_INDENT) {
				// indented code
				if (getActiveBlockParser().getBlock().getType() != Node.Type.Paragraph) {
					offset += CODE_INDENT;
					allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
					container = addChild(new CodeBlockParser(), offset);
				}
				break;
			}

			// this is a little performance optimization:
			if (matchAt(reMaybeSpecial, ln, nextNonSpace) == -1) {
				break;
			}

			offset = nextNonSpace;

			char cc = ln.charAt(offset);

			Matcher matcher;
			if (cc == C_GREATERTHAN) {
				// blockquote
				offset += 1;
				// optional following space
				if (offset < ln.length() && ln.charAt(offset) == C_SPACE) {
					offset++;
				}
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				container = addChild(new BlockQuoteParser(), nextNonSpace);

			} else if ((matcher = reATXHeaderMarker.matcher(ln.substring(offset))).find()) {
				// ATX header
				offset += matcher.group(0).length();
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				int level = matcher.group(0).trim().length(); // number of #s
				// remove trailing ###s:
				String content = ln.substring(offset).replaceAll("^ *#+ *$", "")
						.replaceAll(" +#+ *$", "");
				container = addChild(new HeaderParser(level, content), nextNonSpace);

				break;

			} else if ((matcher = reCodeFence.matcher(ln.substring(offset))).find()) {
				// fenced code block
				int fence_length = matcher.group(0).length();
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				char fenceChar = matcher.group(0).charAt(0);
				container = addChild(new CodeBlockParser(fenceChar, fence_length, indent), nextNonSpace);
				offset += fence_length;
				break;

			} else if (matchAt(reHtmlBlockOpen, ln, offset) != -1) {
				// html block
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				container = addChild(new HtmlBlockParser(), offset);
				offset -= indent; // back up so spaces are part of block
				break;

			} else if (container instanceof ParagraphParser &&
					((ParagraphParser) container).hasSingleLine() &&
					((matcher = reSetextHeaderLine.matcher(ln.substring(offset))).find())) {

				ParagraphParser paragraphParser = (ParagraphParser) container;
				// setext header line
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				int level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
				container = replaceBlock(new HeaderParser(level, paragraphParser.getContentString()));
				offset = ln.length();
				break;

			} else if (matchAt(reHrule, ln, offset) != -1) {
				// hrule
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				container = addChild(new HorizontalRuleParser(), nextNonSpace);
				offset = ln.length() - 1;
				break;

			} else if ((data = parseListMarker(ln, offset, indent)) != null) {
				// list item
				allClosed = allClosed || closeUnmatchedBlocks(unmatchedBlockParsers);
				offset += data.padding;

				// add the list if needed
				if (t != Node.Type.List ||
						!(listsMatch((ListBlock) container.getBlock(), data))) {
					ListBlockParser list = addChild(new ListBlockParser(data), nextNonSpace);
					list.setTight(true);
				}

				// add the list item
				container = addChild(new ListItemParser(data.markerOffset + data.padding), nextNonSpace);

			} else {
				break;
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
				closeUnmatchedBlocks(unmatchedBlockParsers);
			}
			if (blank && container.getBlock().getLastChild() != null) {
				setLastLineBlank(container.getBlock().getLastChild(), true);
			}

			Block block = container.getBlock();
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
			Node cont = container.getBlock();
			while (cont != null) {
				setLastLineBlank(cont, lastLineBlank);
				cont = cont.getParent();
			}

			switch (t) {
				case HtmlBlock:
				case CodeBlock:
					this.addLine(ln, offset);
					break;

				case Header:
				case HorizontalRule:
					// nothing to do; we already added the contents.
					break;

				default:
					if (container.acceptsLine()) {
						this.addLine(ln, nextNonSpace);
					} else if (blank) {
						break;
					} else {
						// create paragraph container for line
						// foo: in JS, there's a third argument, which looks like a bug
						addChild(new ParagraphParser(), nextNonSpace);
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

	private static final String[] tabSpaces = new String[] { "    ", "   ", "  ", " " };

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
			for (int i = blockParsers.size() -1; i >= lastList; i--) {
				BlockParser blockParser = blockParsers.get(i);
				finalize(blockParser, lineNumber);
			}
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
	private <T extends BlockParser> T addChild(T blockParser, int offset) {
		while (!getActiveBlockParser().canContain(blockParser.getBlock().getType())) {
			this.finalize(getActiveBlockParser(), this.lineNumber - 1);
		}

		blockParser.getBlock().setSourcePosition(getSourcePos(offset));
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

	private SourcePosition getSourcePos(int offset) {
		int column_number = offset + 1; // offset 0 = column 1
		return new SourcePosition(this.lineNumber, column_number);
	}

	private boolean isLastLineBlank(Node node) {
		Boolean value = lastLineBlank.get(node);
		return value != null && value;
	}

	private void setLastLineBlank(Node node, boolean value) {
		lastLineBlank.put(node, value);
	}

	// Parse a list marker and return data on the marker (type,
	// start, delimiter, bullet character, padding) or null.
	private static ListData parseListMarker(String ln, int offset, int indent) {
		String rest = ln.substring(offset);
		Matcher match;
		int spaces_after_marker;
		ListData data = new ListData(indent);
		if (reHrule.matcher(rest).find()) {
			return null;
		}
		if ((match = reBulletListMarker.matcher(rest)).find()) {
			spaces_after_marker = match.group(1).length();
			data.type = ListBlock.ListType.BULLET;
			data.bulletChar = match.group(0).charAt(0);

		} else if ((match = reOrderedListMarker.matcher(rest)).find()) {
			spaces_after_marker = match.group(3).length();
			data.type = ListBlock.ListType.ORDERED;
			data.start = Integer.parseInt(match.group(1));
			data.delimiter = match.group(2).charAt(0);
		} else {
			return null;
		}
		boolean blank_item = match.group(0).length() == rest.length();
		if (spaces_after_marker >= 5 ||
				spaces_after_marker < 1 ||
				blank_item) {
			data.padding = match.group(0).length() - spaces_after_marker + 1;
		} else {
			data.padding = match.group(0).length();
		}
		return data;
	}

	// Returns true if the two list items are of the same type,
	// with the same delimiter and bullet character. This is used
	// in agglomerating list items into lists.
	private boolean listsMatch(ListBlock list, ListData item_data) {
		return (Objects.equals(list.getListType(), item_data.type) &&
				list.getOrderedDelimiter() == item_data.delimiter &&
				list.getBulletMarker() == item_data.bulletChar);
	}

	// Finalize and close any unmatched blocks. Returns true.
	// foo: lol?
	private boolean closeUnmatchedBlocks(List<BlockParser> blockParsers) {
		// finalize any blocks not matched
		for (int i = blockParsers.size() - 1; i >= 0; i--) {
			BlockParser blockParser = blockParsers.get(i);
			finalize(blockParser, lineNumber - 1);
		}
		return true;
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

}
