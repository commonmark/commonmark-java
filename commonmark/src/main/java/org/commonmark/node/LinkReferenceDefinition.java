package org.commonmark.node;

/**
 * A link reference definition, e.g.:
 * <pre><code>
 * [foo]: /url "title"
 * </code></pre>
 * <p>
 * They can be referenced anywhere else in the document to produce a link using <code>[foo]</code>. The definitions
 * themselves are usually not rendered in the final output.
 *
 * @see <a href="https://spec.commonmark.org/0.29/#link-reference-definition">Link reference definitions</a>
 */
public class LinkReferenceDefinition extends Node {

	private String label;
    private String destination;
    private String rawDestination;
    private String title;
    private String rawTitle;
    private char delimiterChar;
    private String whitespacePreLabel = "";
    private String whitespacePreDestination = "";
    private String whitespacePreTitle = "";
    private String whitespacePostTitle = "";

    public LinkReferenceDefinition() {
    }

    public LinkReferenceDefinition(String label, String destination, String rawDestination, String title, String rawTitle, char delimiterChar, String whitespacePreLabel, String whitespacePreDestination, String whitespacePreTitle, String whitespacePostTitle) {
        this.label = label;
        this.destination = destination;
        this.rawDestination = rawDestination;
        this.title = title;
        this.rawTitle = rawTitle;
        this.delimiterChar = delimiterChar;
        this.whitespacePreLabel = whitespacePreLabel;
        this.whitespacePreDestination = whitespacePreDestination;
        this.whitespacePreTitle = whitespacePreTitle;
        this.whitespacePostTitle = whitespacePostTitle;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getRawDestination() {
        return rawDestination;
    }

    public void setRawDestination(String rawDestination) {
        this.rawDestination = rawDestination;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getRawTitle() {
        return rawTitle;
    }

    public void setRawTitle(String rawTitle) {
        this.rawTitle = rawTitle;
    }

    public char getDelimiterChar() {
        return delimiterChar;
    }
    
    public String whitespacePreLabel() {
        return whitespacePreLabel;
    }

    public String whitespacePreDestination() {
        return whitespacePreDestination;
    }

    public String whitespacePreTitle() {
        return whitespacePreTitle;
    }

    public String whitespacePostTitle() {
        return whitespacePostTitle;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
