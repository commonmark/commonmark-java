package com.atlassian.rstocker.cm;

import java.util.List;

public class Header extends Block {

	private final int level;

	public Header(SourcePos sourcePos, int level) {
		super(Type.Header, sourcePos);
		this.level = level;
	}

	public int getLevel() {
		return level;
	}
}
