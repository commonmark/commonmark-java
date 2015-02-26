package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.HtmlBlock;

public class HtmlBlockParser extends AbstractBlockParser {

	private final HtmlBlock block = new HtmlBlock();
	private final BlockContent content = new BlockContent();

	@Override
	public ContinueResult continueBlock(String line, int nextNonSpace, int[] offset, boolean blank) {
		return blank ? ContinueResult.NOT_MATCHED : ContinueResult.MATCHED;
	}

	@Override
	public void addLine(String line) {
		content.add(line);
	}

	@Override
	public void finalizeBlock(InlineParser inlineParser) {
		block.setLiteral(content.getString());
	}

	@Override
	public Block getBlock() {
		return block;
	}
}
