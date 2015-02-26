package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.Header;

public class HeaderParser extends AbstractBlockParser {

	private final Header block = new Header();
	private final String content;

	public HeaderParser(int level, String content) {
		block.setLevel(level);
		this.content = content;
	}

	@Override
	public ContinueResult continueBlock(String line, int nextNonSpace, int[] offset, boolean blank) {
		// a header can never container > 1 line, so fail to match
		return ContinueResult.NOT_MATCHED;
	}

	@Override
	public void processInlines(InlineParser inlineParser) {
		inlineParser.parse(block, content);
	}

	@Override
	public Block getBlock() {
		return block;
	}
}
