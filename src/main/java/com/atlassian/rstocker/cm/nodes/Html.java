package com.atlassian.rstocker.cm.nodes;

public class Html extends Node {

	private String literal;

	@Override
	public Type getType() {
		return Type.Html;
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
