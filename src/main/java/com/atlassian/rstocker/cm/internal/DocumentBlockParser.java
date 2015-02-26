package com.atlassian.rstocker.cm.internal;

import com.atlassian.rstocker.cm.node.Block;
import com.atlassian.rstocker.cm.node.Document;
import com.atlassian.rstocker.cm.node.Node;

public class DocumentBlockParser extends AbstractBlockParser {

	private final Document document = new Document();

	@Override
	public ContinueResult continueBlock(String line, int nextNonSpace, int[] offset, boolean blank) {
		return ContinueResult.MATCHED;
	}

	@Override
	public void addLine(String line) {
	}

	@Override
	public boolean canContain(Node.Type type) {
		return true;
	}

	@Override
	public Document getBlock() {
		return document;
	}
}
