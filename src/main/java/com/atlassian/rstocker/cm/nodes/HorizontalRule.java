package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.SourcePos;

public class HorizontalRule extends Block {
	public HorizontalRule(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.HorizontalRule;
	}
}
