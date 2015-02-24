package com.atlassian.rstocker.cm.node;

public class HtmlBlock extends Block {

	private String literal;

	@Override
	public Type getType() {
		return Type.HtmlBlock;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
}
