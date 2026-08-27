package org.commonmark.node;

/**
 * A link with a destination and an optional title; the link text is in child nodes.
 *
 * <p>Example for an inline link in a CommonMark document:
 *
 * <pre><code>
 * [link](/uri "title")
 * </code></pre>
 *
 * <p>The corresponding InlineLink node would look like this:
 *
 * <ul>
 *   <li>{@link #getDestination()} returns {@code "/uri"}
 *   <li>{@link #getTitle()} returns {@code "title"}
 *   <li>A {@link Text} child node with {@link Text#getLiteral() getLiteral} that returns {@code
 *       "link"}
 * </ul>
 *
 * <p>Note that the text in the link can contain inline formatting, so it could also contain an
 * {@link InlineImage} or {@link Emphasis}, etc.
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#links">CommonMark Spec</a>
 */
public class InlineLink extends Node implements Link {

    private String destination;
    private String title;

    public InlineLink() {}

    public InlineLink(String destination, String title) {
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

    /**
     * @return the title or null
     */
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
