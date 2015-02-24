package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.SourcePos;

public class BlockQuote extends Block {
	public BlockQuote(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.BlockQuote;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
