package org.commonmark.node;

/**
 * A paragraph block, contains inline nodes such as {@link Text}
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#paragraphs">CommonMark Spec</a>
 */
public class Paragraph extends Block {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
