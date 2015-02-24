package com.atlassian.rstocker.cm;

import com.atlassian.rstocker.cm.internal.BlockParser;
import com.atlassian.rstocker.cm.node.*;

public class Parser {

	private final BlockParser blockParser;

	public static Builder builder() {
		return new Builder();
	}

	private Parser(BlockParser blockParser) {
		this.blockParser = blockParser;
	}

	public Node parse(String input) {
		return blockParser.parse(input);
	}

	public static class Builder {
		public Parser build() {
			return new Parser(new BlockParser());
		}
	}
}
