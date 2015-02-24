package com.atlassian.rstocker.cm.node;

public class Header extends Block {

	private final int level;

	public Header(int level) {
		this.level = level;
	}

	@Override
	public Type getType() {
		return Type.Header;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public int getLevel() {
		return level;
	}
}
