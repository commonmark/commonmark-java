package org.commonmark.internal;

import org.commonmark.node.Document;
import org.commonmark.node.Node;

public class DocumentBlockParser extends AbstractBlockParser {

	private final Document document = new Document();

	@Override
	public ContinueResult continueBlock(String line, int nextNonSpace, int offset, boolean blank) {
		return blockMatched(offset);
	}

	@Override
	public void addLine(String line) {
	}

	@Override
	public boolean canContain(Node.Type type) {
		return true;
	}

	@Override
	public boolean shouldTryBlockStarts() {
		return true;
	}

	@Override
	public Document getBlock() {
		return document;
	}
}
