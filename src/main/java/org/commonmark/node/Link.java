package org.commonmark.node;

public class Link extends Node {

    private final String destination;
    private final String title;

    public Link(String destination, String title) {
        this.destination = destination;
        this.title = title;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getDestination() {
        return destination;
    }

    public String getTitle() {
        return title;
    }
}
