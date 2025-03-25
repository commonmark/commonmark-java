package org.commonmark.node;

/**
 * A text node, e.g. in:
 * <pre>
 * foo *bar*
 * </pre>
 * <p>
 * The <code>foo </code> is a text node, and the <code>bar</code> inside the emphasis is also a text node.
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#textual-content">CommonMark Spec</a>
 */
public class Text extends Node {

    private String literal;

    public Text() {
    }

    public Text(String literal) {
        this.literal = literal;
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

    @Override
    protected String toStringAttributes() {
        return "literal=" + literal;
    }
}
