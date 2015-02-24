package com.atlassian.rstocker.cm.node;

import com.atlassian.rstocker.cm.SourcePos;

public class CodeBlock extends Block {

	private final char fenceChar;
	private final int fenceLength;
	private final int fenceOffset;

	private String info;
	private String literal;

	// TODO: Split into two classes?
	public CodeBlock(SourcePos sourcePos, char fenceChar, int fenceLength, int fenceOffset) {
		super(sourcePos);
		this.fenceChar = fenceChar;
		this.fenceLength = fenceLength;
		this.fenceOffset = fenceOffset;
	}

	@Override
	public Type getType() {
		return Type.CodeBlock;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public CodeBlock(SourcePos sourcePos) {
		this(sourcePos, '\0', 0, 0);
	}

	public boolean isFenced() {
		return fenceLength > 0;
	}

	public char getFenceChar() {
		return fenceChar;
	}

	public int getFenceLength() {
		return fenceLength;
	}

	public int getFenceOffset() {
		return fenceOffset;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
}
