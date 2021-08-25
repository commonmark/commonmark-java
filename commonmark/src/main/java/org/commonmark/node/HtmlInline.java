package org.commonmark.node;

/**
 * Inline HTML element.
 *
 * @see <a href="http://spec.commonmark.org/0.24/#raw-html">CommonMark Spec</a>
 */
public class HtmlInline extends Node {

    private String literal;
    private String raw;

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
    
    public String getRaw() {
        return raw;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
}
