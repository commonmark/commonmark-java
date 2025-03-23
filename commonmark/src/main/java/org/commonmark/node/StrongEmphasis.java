package org.commonmark.node;

/**
 * Strong emphasis, e.g.:
 * <pre><code>
 * Some **strong emphasis** or __strong emphasis__
 * </code></pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#emphasis-and-strong-emphasis">CommonMark Spec: Emphasis and strong emphasis</a>
 */
public class StrongEmphasis extends Node implements Delimited {

    private String delimiter;

    public StrongEmphasis() {
    }

    public StrongEmphasis(String delimiter) {
        this.delimiter = delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String getOpeningDelimiter() {
        return delimiter;
    }

    @Override
    public String getClosingDelimiter() {
        return delimiter;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
