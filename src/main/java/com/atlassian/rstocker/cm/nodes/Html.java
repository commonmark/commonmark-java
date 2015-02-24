package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.nodes.Node;

public class Html extends Node {

	private String literal;

	@Override
	public Type getType() {
		return Type.Html;
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
}
