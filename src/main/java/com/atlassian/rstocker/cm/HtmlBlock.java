package com.atlassian.rstocker.cm;

public class HtmlBlock extends Block {

	private String literal;

	public HtmlBlock(SourcePos sourcePos) {
		super(Type.HtmlBlock, sourcePos);
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
}
