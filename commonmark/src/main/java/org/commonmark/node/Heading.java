package org.commonmark.node;

/**
 * A heading, e.g.:
 * <pre>
 * First heading
 * =============
 *
 * ## Another heading
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#atx-headings">CommonMark Spec: ATX headings</a>
 * @see <a href="https://spec.commonmark.org/0.31.2/#setext-headings">CommonMark Spec: Setext headings</a>
 */
public class Heading extends Block {

    private int level;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
