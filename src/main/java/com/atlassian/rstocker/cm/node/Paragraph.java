package com.atlassian.rstocker.cm.node;

import com.atlassian.rstocker.cm.SourcePos;

public class Paragraph extends Block {
	public Paragraph(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.Paragraph;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
