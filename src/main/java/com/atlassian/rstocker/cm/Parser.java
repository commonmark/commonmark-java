package com.atlassian.rstocker.cm;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.rstocker.cm.Node.Type;

import static com.atlassian.rstocker.cm.Common.unescapeString;

public class Parser {

	private static char C_GREATERTHAN = 62;
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

	private static Pattern reTrailingBlankLines = Pattern.compile("(?:\n[ \t]*)+$");

	private static Pattern reBulletListMarker = Pattern.compile("^[*+-]( +|$)");

	private static Pattern reOrderedListMarker = Pattern.compile("^(\\d+)([.)])( +|$)");

	private static Pattern reATXHeaderMarker = Pattern.compile("^#{1,6}(?: +|$)");

	private static Pattern reCodeFence = Pattern.compile("^`{3,}(?!.*`)|^~{3,}(?!.*~)");

	private static Pattern reClosingCodeFence = Pattern.compile("^(?:`{3,}|~{3,})(?= *$)");

	private static Pattern reSetextHeaderLine = Pattern.compile("^(?:=+|-+) *$");

	private static Pattern reLineEnding = Pattern.compile("\r\n|\n|\r");

	private Block doc;
	private Block tip;
	private Block oldtip;
	private Map<String, Link> refmap;
	private int lineNumber = 0;
	private Node lastMatchedContainer;
	private int lastLineLength = 0;
	private InlineParser inlineParser = new InlineParser();

	private Set<Block> openBlocks = new HashSet<>();
	private Map<ListItem, Integer> listItemOffset = new HashMap<>();
	private Map<Block, BlockContent> blockContent = new HashMap<>();

	private Parser(Builder builder) {
	}

	public static Builder builder() {
		return new Builder();
	}

	// The main parsing function. Returns a parsed document AST.
	public Node parse(String input) {
		this.doc = document();
		this.tip = this.doc;
		this.refmap = new HashMap<>();
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
		while (this.tip != null) {
			this.finalize(this.tip, len);
		}
		// if (this.options.time) { console.timeEnd("block parsing"); }
		// if (this.options.time) { console.time("inline parsing"); }
		this.processInlines(this.doc);
		// if (this.options.time) { console.timeEnd("inline parsing"); }
		return this.doc;
	}

	// Walk through a block & children recursively, parsing string content
	// into inline content where appropriate. Returns new object.
	private void processInlines(Node block) {
		Node.NodeWalker walker = block.walker();
		Node.NodeWalker.Entry entry;
		while ((entry = walker.next()) != null) {
			Node node = entry.node;
			Type t = node.type();
			if (!entry.entering && (t == Type.Paragraph || t == Type.Header)) {
				this.inlineParser.parse(node, getContent((Block) node).getString(), this.refmap);
			}
		}
	}

	private Document document() {
		return new Document(new SourcePos(1, 1));
	}

	// Analyze a line of text and update the document appropriately.
	// We parse markdown text by calling this on each line of input,
	// then finalizing the document.
	private void incorporateLine(String ln) {
		boolean all_matched = true;
		int first_nonspace;
		int offset = 0;
		int match;
		ListData data;
		boolean blank = false;
		int indent = 0;
		int i;
		int CODE_INDENT = 4;
		boolean allClosed;

		Block container = this.doc;
		this.oldtip = this.tip;
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
		while (container.lastChild != null && container.lastChild instanceof Block && openBlocks.contains(container.lastChild)) {
			container = (Block) container.lastChild;

			match = matchAt(reNonSpace, ln, offset);
			if (match == -1) {
				first_nonspace = ln.length();
				blank = true;
			} else {
				first_nonspace = match;
				blank = false;
			}
			indent = first_nonspace - offset;

			switch (container.type()) {
			case BlockQuote:
				if (indent <= 3 && first_nonspace < ln.length() && ln.charAt(first_nonspace) == C_GREATERTHAN) {
					offset = first_nonspace + 1;
					if (offset < ln.length() && ln.charAt(offset) == C_SPACE) {
						offset++;
					}
				} else {
					all_matched = false;
				}
				break;

			case Item:
				ListItem item = (ListItem) container;
				if (blank) {
					offset = first_nonspace;
				} else if (indent >= listItemOffset.get(item)) {
					offset += listItemOffset.get(item);
				} else {
					all_matched = false;
				}
				break;

			case Header:
			case HorizontalRule:
				// a header can never container > 1 line, so fail to match:
				all_matched = false;
				break;

			case CodeBlock:
				CodeBlock codeBlock = (CodeBlock) container;
				if (codeBlock.isFenced()) { // fenced
					Matcher matcher = null;
					boolean matches = (indent <= 3 &&
							first_nonspace < ln.length() &&
							ln.charAt(first_nonspace) == codeBlock.getFenceChar() &&
							(matcher = reClosingCodeFence.matcher(ln.substring(first_nonspace)))
									.find());
					if (matches && matcher.group(0).length() >= codeBlock.getFenceLength()) {
						// closing fence - we're at end of line, so we can return
						all_matched = false;
						this.finalize(codeBlock, this.lineNumber);
						this.lastLineLength = ln.length() - 1; // -1 for newline
						return;
					} else {
						// skip optional spaces of fence offset
						i = codeBlock.getFenceOffset();
						while (i > 0 && ln.charAt(offset) == C_SPACE) {
							offset++;
							i--;
						}
					}
				} else { // indented
					if (indent >= CODE_INDENT) {
						offset += CODE_INDENT;
					} else if (blank) {
						offset = first_nonspace;
					} else {
						all_matched = false;
					}
				}
				break;

			case HtmlBlock:
				if (blank) {
					all_matched = false;
				}
				break;

			case Paragraph:
				if (blank) {
					all_matched = false;
				}
				break;

			default:
			}

			if (!all_matched) {
				container = container.getParent(); // back up to last matching block
				break;
			}
		}

		allClosed = (container == this.oldtip);
		this.lastMatchedContainer = container;

		// Check to see if we've hit 2nd blank line; if so break out of list:
		if (blank && container.lastLineBlank) {
			this.breakOutOfLists(container);
		}

		// Unless last matched container is a code block, try new container starts,
		// adding children to the last matched container:
		while (true) {
			Type t = container.type();

			match = matchAt(reNonSpace, ln, offset);
			if (match == -1) {
				first_nonspace = ln.length();
				blank = true;
				break;
			} else {
				first_nonspace = match;
				blank = false;
			}
			indent = first_nonspace - offset;

			if (t == Type.CodeBlock || t == Type.HtmlBlock) {
				break;
			}

			if (indent >= CODE_INDENT) {
				// indented code
				if (this.tip.type() != Type.Paragraph && !blank) {
					offset += CODE_INDENT;
					allClosed = allClosed ||
							this.closeUnmatchedBlocks();
					container = addChild(new CodeBlock(getSourcePos(offset)));
				}
				break;
			}

			// this is a little performance optimization:
			if (matchAt(reMaybeSpecial, ln, first_nonspace) == -1) {
				break;
			}

			offset = first_nonspace;

			char cc = ln.charAt(offset);

			Matcher matcher;
			if (cc == C_GREATERTHAN) {
				// blockquote
				offset += 1;
				// optional following space
				if (offset < ln.length() && ln.charAt(offset) == C_SPACE) {
					offset++;
				}
				allClosed = allClosed || this.closeUnmatchedBlocks();
				container = addChild(new BlockQuote(getSourcePos(first_nonspace)));

			} else if ((matcher = reATXHeaderMarker.matcher(ln.substring(offset))).find()) {
				// ATX header
				offset += matcher.group(0).length();
				allClosed = allClosed || this.closeUnmatchedBlocks();
				int level = matcher.group(0).trim().length(); // number of #s
				Header header = addChild(new Header(getSourcePos(first_nonspace), level));
				container = header;

				// remove trailing ###s:
				String stripped = ln.substring(offset).replaceAll("^ *#+ *$", "")
						.replaceAll(" +#+ *$", "");
				addLine(stripped, 0);
				break;

			} else if ((matcher = reCodeFence.matcher(ln.substring(offset))).find()) {
				// fenced code block
				int fence_length = matcher.group(0).length();
				allClosed = allClosed || this.closeUnmatchedBlocks();
				char fenceChar = matcher.group(0).charAt(0);
				CodeBlock codeBlock = addChild(new CodeBlock(getSourcePos(first_nonspace), fenceChar, fence_length, indent));
				container = codeBlock;
				offset += fence_length;
				break;

			} else if (matchAt(reHtmlBlockOpen, ln, offset) != -1) {
				// html block
				allClosed = allClosed || this.closeUnmatchedBlocks();
				container = addChild(new HtmlBlock(getSourcePos(offset)));
				offset -= indent; // back up so spaces are part of block
				break;

			} else if (t == Type.Paragraph &&
					getContent(container).hasSingleLine() &&
					((matcher = reSetextHeaderLine.matcher(ln.substring(offset))).find())) {

				Paragraph paragraph = (Paragraph) container;
				// setext header line
				allClosed = allClosed || this.closeUnmatchedBlocks();
				int level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
				Header header = new Header(paragraph.getSourcePos(), level);
				blockContent.put(header, getContent(paragraph));
				container.insertAfter(header);
				container.unlink();
				container = header;
				this.tip = header;
				offset = ln.length();
				break;

			} else if (matchAt(reHrule, ln, offset) != -1) {
				// hrule
				allClosed = allClosed || this.closeUnmatchedBlocks();
				container = addChild(new HorizontalRule(getSourcePos(first_nonspace)));
				offset = ln.length() - 1;
				break;

			} else if ((data = parseListMarker(ln, offset, indent)) != null) {
				// list item
				allClosed = allClosed || this.closeUnmatchedBlocks();
				offset += data.padding;

				// add the list if needed
				if (t != Type.List ||
						!(listsMatch((ListBlock) container, data))) {
					ListBlock list = addChild(new ListBlock(getSourcePos(first_nonspace), data.type, data.delimiter, data.start, data.bulletChar));
					list.setTight(true);
				}

				// add the list item
				ListItem listItem = addChild(new ListItem(getSourcePos(first_nonspace)));
				listItemOffset.put(listItem, data.markerOffset + data.padding);
				container = listItem;

			} else {
				break;

			}

		}

		// What remains at the offset is a text line. Add the text to the
		// appropriate container.

		// First check for a lazy paragraph continuation:
		if (!allClosed && !blank &&
				this.tip.type() == Type.Paragraph &&
				blockContent.get(tip).hasLines()) {
			// lazy paragraph continuation

			// foo: on DocParser? Looks like an error to me
			// this.last_line_blank = false;
			this.addLine(ln, offset);

		} else { // not a lazy continuation

			// finalize any blocks not matched
			allClosed = allClosed || this.closeUnmatchedBlocks();
			if (blank && container.lastChild != null) {
				container.lastChild.lastLineBlank = true;
			}

			Type t = container.type();

			// Block quote lines are never blank as they start with >
			// and we don't count blanks in fenced code for purposes of tight/loose
			// lists or breaking out of lists. We also don't set last_line_blank
			// on an empty list item, or if we just closed a fenced block.
			boolean lastLineBlank = blank &&
					!(t == Type.BlockQuote ||
							(t == Type.CodeBlock && ((CodeBlock) container).isFenced()) ||
					(t == Type.Item &&
							container.firstChild == null &&
							container.getSourcePos().getStartLine() == this.lineNumber));

			// propagate lastLineBlank up through parents:
			Node cont = container;
			while (cont != null) {
				cont.lastLineBlank = lastLineBlank;
				cont = cont.parent;
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
				if (acceptsLines(t)) {
					this.addLine(ln, first_nonspace);
				} else if (blank) {
					break;
				} else {
					// create paragraph container for line
					// foo: in JS, there's a third argument, which looks like a bug
					addChild(new Paragraph(getSourcePos(first_nonspace)));
					this.addLine(ln, first_nonspace);
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
	private void finalize(Block block, int lineNumber) {
		// foo: top? looks like a bug
		// var above = block.parent || this.top;

		Block above = block.getParent();
		openBlocks.remove(block);
		block.setSourcePos(new SourcePos(block.getSourcePos().getStartLine(), block.getSourcePos().getStartColumn(), lineNumber, this.lastLineLength + 1));

		switch (block.type()) {
		case Paragraph: {
			int pos;
			String content = getContent(block).getString();

			// try parsing the beginning as link reference definitions:
			while (content.charAt(0) == C_OPEN_BRACKET &&
					(pos = this.inlineParser.parseReference(content,
							this.refmap)) != 0) {
				content = content.substring(pos);
				if (isBlank(content)) {
					block.unlink();
					break;
				}
			}
			blockContent.put(block, new BlockContent(content));
			break;
		}
		case HtmlBlock:
			block.literal = getContent(block).getString();
			break;

		case CodeBlock: {
			CodeBlock codeBlock = (CodeBlock) block;
			BlockContent content = getContent(codeBlock);
			boolean singleLine = content.hasSingleLine();
			// add trailing newline
			content.add("");
			String contentString = content.getString();

			if (codeBlock.isFenced()) { // fenced
				// first line becomes info string
				int firstNewline = contentString.indexOf('\n');
				String firstLine = contentString.substring(0, firstNewline);
				block.info = unescapeString(firstLine.trim());
				if (singleLine) {
					block.literal = "";
				} else {
					String literal = contentString.substring(firstNewline + 1);
					block.literal = literal;
				}
			} else { // indented
				String literal = reTrailingBlankLines.matcher(contentString).replaceFirst("\n");
				block.literal = literal;
			}
			break;
		}
		case List: {
			ListBlock list = (ListBlock) block;
			Node item = block.firstChild;
			while (item != null) {
				// check for non-final list item ending with blank line:
				if (endsWithBlankLine(item) && item.next != null) {
					list.setTight(false);
					break;
				}
				// recurse into children of list item, to see if there are
				// spaces between any of them:
				Node subitem = item.firstChild;
				while (subitem != null) {
					if (endsWithBlankLine(subitem) && (item.next != null || subitem.next != null)) {
						list.setTight(false);
						break;
					}
					subitem = subitem.next;
				}
				item = item.next;
			}
			break;
		}
		default:
			break;
		}

		this.tip = above;
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
	private void breakOutOfLists(Block block) {
		Block b = block;
		Block last_list = null;
		do {
			if (b.type() == Type.List) {
				last_list = b;
			}
			b = b.getParent();
		} while (b != null);

		if (last_list != null) {
			while (block != last_list) {
				this.finalize(block, this.lineNumber);
				block = block.getParent();
			}
			this.finalize(last_list, this.lineNumber);
			this.tip = last_list.getParent();
		}
	}

	// Add a line to the block at the tip. We assume the tip
	// can accept lines -- that check should be done before calling this.
	private void addLine(String ln, int offset) {
		getContent(tip).add(ln.substring(offset));
	}

	private BlockContent getContent(Block block) {
		return blockContent.get(block);
	}

	// Add block of type tag as a child of the tip. If the tip can't
	// accept children, close and finalize it and try its parent,
	// and so on til we find a block that can accept children.
	private <T extends Block> T addChild(T node) {
		openBlocks.add(node);
		while (!canContain(this.tip.type(), node.type())) {
			this.finalize(this.tip, this.lineNumber - 1);
		}

		blockContent.put(node, new BlockContent());
		this.tip.appendChild(node);
		this.tip = node;
		return node;
	}

	private SourcePos getSourcePos(int offset) {
		int column_number = offset + 1; // offset 0 = column 1
		return new SourcePos(this.lineNumber, column_number);
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
	private boolean closeUnmatchedBlocks() {
		// finalize any blocks not matched
		while (this.oldtip != this.lastMatchedContainer) {
			Block parent = this.oldtip.getParent();
			this.finalize(this.oldtip, this.lineNumber - 1);
			this.oldtip = parent;
		}
		return true;
	}

	// Returns true if parent block can contain child block.
	private static boolean canContain(Type parent_type, Type child_type) {
		return (parent_type == Type.Document ||
				parent_type == Type.BlockQuote ||
				parent_type == Type.Item || (parent_type == Type.List && child_type == Type.Item));
	}

	// Returns true if block type can accept lines of text.
	private static boolean acceptsLines(Type block_type) {
		return block_type == Type.Paragraph ||
				block_type == Type.CodeBlock;
	}

	// Returns true if string contains only space characters.
	private static boolean isBlank(String s) {
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

	// Returns true if block ends with a blank line, descending if needed
	// into lists and sublists.
	private static boolean endsWithBlankLine(Node block) {
		while (block != null) {
			if (block.lastLineBlank) {
				return true;
			}
			Type t = block.type();
			if (t == Type.List || t == Type.Item) {
				block = block.lastChild;
			} else {
				break;
			}
		}
		return false;
	}

	public static class Builder {
		public Parser build() {
			return new Parser(this);
		}
	}
}
