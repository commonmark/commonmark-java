package com.atlassian.rstocker.cm.node;

/**
 * Inline HTML element.
 *
 * @see <a href="http://spec.commonmark.org/0.18/#raw-html">CommonMark Spec</a>
 */
public class Html extends Node {

	private String literal;

	@Override
	public Type getType() {
		return Type.Html;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}
}
