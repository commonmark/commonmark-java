package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.BlockQuote;
import com.atlassian.rstocker.cm.node.Node;

public class BlockQuoteParser extends AbstractBlockParser {

	private final BlockQuote block = new BlockQuote();

	@Override
	public boolean canContain(Node.Type type) {
		return true;
	}

	@Override
	public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
		int indent = nextNonSpace - offset;
		if (indent <= 3 && nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
			int newOffset = nextNonSpace + 1;
			if (newOffset < line.length() && line.charAt(newOffset) == ' ') {
				newOffset++;
			}
			return blockMatched(newOffset);
		} else {
			return blockDidNotMatch();
		}
	}

	@Override
	public BlockQuote getBlock() {
		return block;
	}
}
