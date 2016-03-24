package org.commonmark.node;

public class AutoLink extends Link {

    public AutoLink() {
        super();
    }

    public AutoLink(String destination, String title) {
        super(destination, title);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
