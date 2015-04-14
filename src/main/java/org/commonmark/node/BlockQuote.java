package org.commonmark.node;

public class BlockQuote extends Block {

	@Override
	public Type getType() {
		return Type.BlockQuote;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
