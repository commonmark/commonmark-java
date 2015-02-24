package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.SourcePos;

public class ListItem extends Block {
	public ListItem(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.Item;
	}
}
