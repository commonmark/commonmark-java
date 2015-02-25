package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Node;

public abstract class AbstractBlockParser implements BlockParser {

	private boolean lastLineBlank = false;

	@Override
	public void finalizeBlock(InlineParser inlineParser) {
	}

	@Override
	public void processInlines(InlineParser inlineParser) {
	}

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
	public void setLastLineBlank(boolean lastLineBlank) {
		this.lastLineBlank = lastLineBlank;
	}

	@Override
	public boolean isLastLineBlank() {
		return lastLineBlank;
	}
}
