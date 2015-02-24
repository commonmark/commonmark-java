package com.atlassian.rstocker.cm.node;

public class ListItem extends Block {

	@Override
	public Type getType() {
		return Type.Item;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
