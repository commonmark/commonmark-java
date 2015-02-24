package com.atlassian.rstocker.cm.node;

public class Emphasis extends Node {
	@Override
	public Type getType() {
		return Type.Emph;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
