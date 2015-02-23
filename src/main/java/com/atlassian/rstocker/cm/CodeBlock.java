package com.atlassian.rstocker.cm;

public class CodeBlock extends Block {

	private final char fenceChar;
	private final int fenceLength;
	private final int fenceOffset;

	public CodeBlock(SourcePos sourcePos, char fenceChar, int fenceLength, int fenceOffset) {
		super(Type.CodeBlock, sourcePos);
		this.fenceChar = fenceChar;
		this.fenceLength = fenceLength;
		this.fenceOffset = fenceOffset;
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
}
