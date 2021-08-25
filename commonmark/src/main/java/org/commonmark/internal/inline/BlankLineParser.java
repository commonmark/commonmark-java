package org.commonmark.internal.inline;

import org.commonmark.node.BlankLine;
import org.commonmark.node.Block;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.ParserState;

public class BlankLineParser extends AbstractBlockParser {
	
	private BlankLine block;
	
	public BlankLineParser(String raw) {
		block = new BlankLine(raw);
	}

	@Override
	public Block getBlock() {
		return block;
	}

	@Override
	public BlockContinue tryContinue(ParserState parserState) {
		// Blank lines do not become active parsers, so they cannot be continued
		return null;
	}

}