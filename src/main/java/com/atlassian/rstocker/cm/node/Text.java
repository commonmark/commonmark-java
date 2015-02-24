package com.atlassian.rstocker.cm.node;

public class Text extends Node {

	private String literal;

	@Override
	public Type getType() {
		return Type.Text;
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
