package org.commonmark.node;

/**
 * A block quote, e.g.:
 * <pre>
 * &gt; Some quoted text
 * </pre>
 * <p>
 * Note that child nodes are themselves blocks, e.g. {@link Paragraph}, {@link ListBlock} etc.
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#block-quotes">CommonMark Spec</a>
 */
public class BlockQuote extends Block {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
