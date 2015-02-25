package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.BlockQuote;
import com.atlassian.rstocker.cm.node.Node;

public class BlockQuoteParser extends AbstractBlockParser {

	private final BlockQuote block = new BlockQuote();

	@Override
	public ContinueResult parseLine(String line, int nextNonSpace, int[] offset, boolean blank) {
		int indent = nextNonSpace - offset[0];
		if (indent <= 3 && nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
			offset[0] = nextNonSpace + 1;
			if (offset[0] < line.length() && line.charAt(offset[0]) == ' ') {
				offset[0]++;
			}
			return ContinueResult.MATCHED;
		} else {
			return ContinueResult.NOT_MATCHED;
		}
	}

	@Override
	public void addLine(String line) {
		// TODO
	}

	@Override
	public boolean canContain(Node.Type type) {
		return true;
	}

	@Override
	public BlockQuote getBlock() {
		return block;
	}
}
