package org.commonmark.node;

public class Paragraph extends Block {

	@Override
	public Type getType() {
		return Type.Paragraph;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
