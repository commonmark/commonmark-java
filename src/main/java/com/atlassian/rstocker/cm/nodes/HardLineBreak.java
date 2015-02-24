package com.atlassian.rstocker.cm.nodes;

public class HardLineBreak extends Node {
	@Override
	public Type getType() {
		return Type.Hardbreak;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
