package com.atlassian.rstocker.cm;

public class SourcePos {

	private final int startLine;
	private final int startColumn;
	private final int endLine;
	private final int endColumn;

	public SourcePos(int startLine, int startColumn) {
		this(startLine, startColumn, 0, 0);
	}

	public SourcePos(int startLine, int startColumn, int endLine, int endColumn) {
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndColumn() {
		return endColumn;
	}
}
