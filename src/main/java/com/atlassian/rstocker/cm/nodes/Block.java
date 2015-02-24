package com.atlassian.rstocker.cm.nodes;

import com.atlassian.rstocker.cm.SourcePos;

public abstract class Block extends Node {

	private SourcePos sourcePos;

	public Block(SourcePos sourcePos) {
		this.sourcePos = sourcePos;
	}

	public SourcePos getSourcePos() {
		return this.sourcePos;
	}

	public void setSourcePos(SourcePos sourcePos) {
		this.sourcePos = sourcePos;
	}

	public Block getParent() {
		return (Block) super.getParent();
	}

	@Override
	protected void setParent(Node parent) {
		if (!(parent instanceof Block)) {
			throw new IllegalArgumentException("Parent of block must also be block (can not be inline)");
		}
		super.setParent(parent);
	}
}
