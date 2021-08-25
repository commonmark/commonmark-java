package org.commonmark.node;

/**
 * HTML block
 *
 * @see <a href="http://spec.commonmark.org/0.18/#html-blocks">CommonMark Spec</a>
 */
public class HtmlBlock extends Block {
	// Track whitespace as follows:
    //    [0] Pre-block
    //    [1] Pre-content
    //    [2] Post-content
    //    [3] Post-block
    private String[] whitespaceTracker = {"", "", "", ""};

    private String literal;
    private String raw;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }
    
    public String getRaw() {
        return raw;
    }
    
    public void setRaw(String raw) {
        this.raw = raw;
    }
    
    @Override
    public String whitespacePreBlock() {
        return whitespaceTracker[0];
    }

    @Override
    public String whitespacePreContent() {
        return whitespaceTracker[1];
    }

    @Override
    public String whitespacePostContent() {
        return whitespaceTracker[2];
    }

    @Override
    public String whitespacePostBlock() {
        return whitespaceTracker[3];
    }
    
    public void setWhitespace(String... newWhitespace) {
        whitespaceTracker = super.prepareStructuralWhitespace(newWhitespace);
    }
}
