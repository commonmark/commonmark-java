package org.commonmark.node;

/**
 * A link with a destination and an optional title; the link text is in child nodes.
 * <p>
 * Example for an inline link in a CommonMark document:
 * <pre><code>
 * [link](/uri "title")
 * </code></pre>
 * <p>
 * The corresponding Link node would look like this:
 * <ul>
 * <li>{@link #getDestination()} returns {@code "/uri"}
 * <li>{@link #getTitle()} returns {@code "title"}
 * <li>A {@link Text} child node with {@link Text#getLiteral() getLiteral} that returns {@code "link"}</li>
 * </ul>
 * <p>
 * Note that the text in the link can contain inline formatting, so it could also contain an {@link Image} or
 * {@link Emphasis}, etc.
 *
 * @see <a href="http://spec.commonmark.org/0.26/#links">CommonMark Spec for links</a>
 */
public class Link extends Node implements LinkFormat {

    private String destination;
    private String rawDestination;
    private String title;
    private String rawTitle;
    private String label;
    private String rawLabel;
    private LinkType linkType;
    private char titleSymbol;
    private String whitespacePreDestination;
    private String whitespacePreTitle;
    private String whitespacePostContent;

    public Link() {
    }

    public Link(String destination, String title) {
        this.destination = destination;
        this.rawDestination = "";
        this.title = title;
        this.rawTitle = "";
        this.label = "";
        this.rawLabel = "";
        linkType = LinkType.NULL;
        whitespacePreDestination = "";
        whitespacePreTitle = "";
        whitespacePostContent = "";
    }
    
    public Link(String destination, String rawDestination, String title,
            String rawTitle, String label, String rawLabel,
            LinkType linkType, char titleSymbol, String whitespacePreDestination,
            String whitespacePreTitle, String whitespacePostContent) {
        this.destination = destination;
        this.rawDestination = rawDestination;
        this.title = title;
        this.rawTitle = rawTitle;
        this.label = label;
        this.rawLabel = rawLabel;
        this.linkType = linkType;
        this.titleSymbol = titleSymbol;
        this.whitespacePreDestination = whitespacePreDestination;
        this.whitespacePreTitle = whitespacePreTitle;
        this.whitespacePostContent = whitespacePostContent;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getDestination() {
        return destination;
    }

    @Override
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    @Override
    public String getRawDestination() {
        return rawDestination;
    }

    @Override
    public void setRawDestination(String rawDestination) {
        this.rawDestination = rawDestination;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String getRawTitle() {
        return rawTitle;
    }
    
    @Override
    public void setRawTitle(String rawTitle) {
        this.rawTitle = rawTitle;
    }
    
    @Override
    public LinkType getLinkType() {
        return linkType;
    }

    @Override
    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    @Override
    public char getTitleSymbol() {
        return titleSymbol;
    }

    @Override
    public void setTitleSymbol(char titleSymbol) {
        this.titleSymbol = titleSymbol;
    }
    
    @Override
    public String getWhitespacePreDestination() {
        return whitespacePreDestination;
    }

    @Override
    public void setWhitespacePreDestination(String whitespacePreDestination) {
        this.whitespacePreDestination = whitespacePreDestination;
    }

    @Override
    public String getWhitespacePreTitle() {
        return whitespacePreTitle;
    }

    @Override
    public void setWhitespacePreTitle(String whitespacePreTitle) {
        this.whitespacePreTitle = whitespacePreTitle;
    }

    @Override
    public String getWhitespacePostContent() {
        return whitespacePostContent;
    }

    @Override
    public void setWhitespacePostContent(String whitespacePostContent) {
        this.whitespacePostContent = whitespacePostContent;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }
    
    @Override
    public String getRawLabel() {
        return rawLabel;
    }
    
    @Override
    public void setRawLabel(String rawLabel) {
        this.rawLabel = rawLabel;
    }
    
    @Override
    protected String toStringAttributes() {
        return "destination=" + destination + ", title=" + title;
    }
}
