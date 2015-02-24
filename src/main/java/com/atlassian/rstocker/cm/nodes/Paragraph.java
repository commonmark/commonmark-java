package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.SourcePos;

public class Paragraph extends Block {
	public Paragraph(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.Paragraph;
	}
}
