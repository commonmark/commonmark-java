package com.atlassian.rstocker.cm.node;

public class SoftLineBreak extends Node {
	@Override
	public Type getType() {
		return Type.Softbreak;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
