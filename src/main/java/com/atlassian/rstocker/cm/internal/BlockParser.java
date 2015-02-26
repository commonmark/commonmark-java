package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.Node;

public interface BlockParser {

	enum ContinueResult {
		MATCHED,
		NOT_MATCHED,
		FINALIZE
	}

	boolean canContain(Node.Type type);

	ContinueResult continueBlock(String line, int nextNonSpace, int[] offset, boolean blank);

	/** Returns true if block type can accept lines of text */
	boolean acceptsLine();

	void addLine(String line);

	void finalizeBlock(InlineParser inlineParser);

	void processInlines(InlineParser inlineParser);

	Block getBlock();
}
