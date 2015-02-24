package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.SourcePos;

public class Header extends Block {

	private final int level;

	public Header(SourcePos sourcePos, int level) {
		super(sourcePos);
		this.level = level;
	}

	@Override
	public Type getType() {
		return Type.Header;
	}

	public int getLevel() {
		return level;
	}
}
