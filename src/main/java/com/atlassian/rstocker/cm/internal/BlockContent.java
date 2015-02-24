package com.atlassian.rstocker.cm.internal;

class BlockContent {

	private final StringBuilder sb;

	private int lineCount = 0;

	public BlockContent() {
		sb = new StringBuilder();
	}

	public BlockContent(String content) {
		sb = new StringBuilder(content);
	}

	public void add(String line) {
		if (lineCount != 0) {
			sb.append('\n');
		}
		sb.append(line);
		lineCount++;
	}

	public boolean hasSingleLine() {
		return lineCount == 1;
	}

	public boolean hasLines() {
		return lineCount > 0;
	}

	public String getString() {
		return sb.toString();
	}

}
