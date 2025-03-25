package org.commonmark.node;

/**
 * A hard line break, e.g.:
 * <pre>
 * line\
 * break
 * </pre>
 * <p>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#hard-line-breaks">CommonMark Spec</a>
 */
public class HardLineBreak extends Node {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
