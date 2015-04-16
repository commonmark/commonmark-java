package org.commonmark.node;

/**
 * Inline HTML element.
 *
 * @see <a href="http://spec.commonmark.org/0.18/#raw-html">CommonMark Spec</a>
 */
public class HtmlTag extends Node {

    private String literal;

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
