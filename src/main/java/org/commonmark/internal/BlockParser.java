package org.commonmark.internal;

import org.commonmark.node.Block;
import org.commonmark.node.Node;

public interface BlockParser {

	ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank);

	boolean canContain(Node.Type type);

	boolean shouldTryBlockStarts();

	/**
	 * Returns true if block type can accept lines of text
	 */
	boolean acceptsLine();

	void addLine(String line);

	void finalizeBlock(InlineParser inlineParser);

	void processInlines(InlineParser inlineParser);

	Block getBlock();

	interface ContinueResult {
	}

	interface BlockMatched extends ContinueResult {
		int getNewOffset();
	}

	interface BlockDidNotMatch extends ContinueResult {
	}

	interface BlockMatchedAndCanBeFinalized extends ContinueResult {
	}
}
