package com.atlassian.rstocker.cm.node;

public class Document extends Block {
	@Override
	public Type getType() {
		return Type.Document;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
