package org.commonmark.node;

public class BlankLine extends Block {

	private String raw;
	
	public BlankLine(String rawContent) {
		raw = rawContent;
	}

	public String getRaw() {
		return raw;
	}

	public void setRaw(String raw) {
		this.raw = raw;
	}

	@Override
	public String whitespacePreBlock() {
		// Blank lines contain any whitespace as part of their raw content
		return "";
	}

	@Override
	public String whitespacePreContent() {
		// Blank lines contain any whitespace as part of their raw content
		return "";
	}

	@Override
	public String whitespacePostContent() {
		// Blank lines contain any whitespace as part of their raw content
		return "";
	}

	@Override
	public String whitespacePostBlock() {
		// Blank lines contain any whitespace as part of their raw content
		return "";
	}

	@Override
	public void setWhitespace(String... newWhitespace) {
		// Blank lines contain any whitespace as part of their raw content
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}
}
