package org.commonmark.node;

public class Image extends Node {

    private final String destination;
    private final String title;

    public Image(String destination, String title) {
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
