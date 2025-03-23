package org.commonmark.node;

/**
 * A soft line break (as opposed to a {@link HardLineBreak}), e.g. between:
 * <pre>
 * foo
 * bar
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#soft-line-breaks">CommonMark Spec</a>
 */
public class SoftLineBreak extends Node {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
