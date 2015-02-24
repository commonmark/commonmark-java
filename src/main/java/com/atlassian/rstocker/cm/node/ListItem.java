package com.atlassian.rstocker.cm.node;

import com.atlassian.rstocker.cm.SourcePos;

public class ListItem extends Block {
	public ListItem(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.Item;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
