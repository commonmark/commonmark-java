package com.atlassian.rstocker.cm.node;

import com.atlassian.rstocker.cm.SourcePos;

public class Document extends Block {
	public Document(SourcePos sourcePos) {
		super(sourcePos);
	}

	@Override
	public Type getType() {
		return Type.Document;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
