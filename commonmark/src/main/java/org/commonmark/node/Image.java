package org.commonmark.node;

/**
 * An image, e.g.:
 * <pre>
 * ![foo](/url "title")
 * </pre>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#images">CommonMark Spec</a>
 */
public class Image extends Node {

    private String destination;
    private String title;

    public Image() {
    }

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

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    protected String toStringAttributes() {
        return "destination=" + destination + ", title=" + title;
    }
}
