package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Node;

public abstract class AbstractBlockParser implements BlockParser {

	@Override
	public boolean canContain(Node.Type type) {
		return false;
	}

	@Override
	public boolean acceptsLine() {
		return false;
	}

	@Override
	public void addLine(String line) {
	}

	@Override
	public void finalizeBlock(InlineParser inlineParser) {
	}

	@Override
	public void processInlines(InlineParser inlineParser) {
	}
}
