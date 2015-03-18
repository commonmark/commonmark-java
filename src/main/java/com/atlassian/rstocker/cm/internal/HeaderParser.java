package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.Header;
import com.atlassian.rstocker.cm.node.SourcePosition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderParser extends AbstractBlockParser {

	private static Pattern ATX_HEADER = Pattern.compile("^#{1,6}(?: +|$)");
	private static Pattern SETEXT_HEADER = Pattern.compile("^(?:=+|-+) *$");

	private final Header block = new Header();
	private final String content;

	public HeaderParser(int level, String content, SourcePosition pos) {
		block.setLevel(level);
		block.setSourcePosition(pos);
		this.content = content;
	}

	@Override
	public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
		// a header can never container > 1 line, so fail to match
		return blockDidNotMatch();
	}

	@Override
	public void processInlines(InlineParser inlineParser) {
		inlineParser.parse(block, content);
	}

	@Override
	public Block getBlock() {
		return block;
	}

	public static class Factory extends AbstractBlockParserFactory {

		@Override
		public StartResult tryStart(ParserState state) {
			String line = state.getLine();
			int nextNonSpace = state.getNextNonSpace();
			BlockParser activeBlockParser = state.getActiveBlockParser();
			Matcher matcher;
			if ((matcher = ATX_HEADER.matcher(line.substring(nextNonSpace))).find()) {
				// ATX header
				int newOffset = nextNonSpace + matcher.group(0).length();
				int level = matcher.group(0).trim().length(); // number of #s
				// remove trailing ###s:
				String content = line.substring(newOffset).replaceAll("^ *#+ *$", "")
						.replaceAll(" +#+ *$", "");
				return start(new HeaderParser(level, content, pos(state, nextNonSpace)), newOffset, false);

			} else if (activeBlockParser instanceof ParagraphParser &&
					((ParagraphParser) activeBlockParser).hasSingleLine() &&
					((matcher = SETEXT_HEADER.matcher(line.substring(nextNonSpace))).find())) {
				// setext header line

				ParagraphParser paragraphParser = (ParagraphParser) activeBlockParser;
				int level = matcher.group(0).charAt(0) == '=' ? 1 : 2;
				String content = paragraphParser.getContentString();
				return start(new HeaderParser(level, content, paragraphParser.getBlock().getSourcePosition()), nextNonSpace, true);
			} else {
				return noStart();
			}
		}
	}
}
