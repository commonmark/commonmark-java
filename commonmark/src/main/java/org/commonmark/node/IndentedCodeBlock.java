package org.commonmark.node;

/**
 * An indented code block, e.g.:
 * <pre><code>
 * Code follows:
 *
 *     foo
 *     bar
 * </code></pre>
 * <p>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#indented-code-blocks">CommonMark Spec</a>
 */
public class IndentedCodeBlock extends Block {

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
