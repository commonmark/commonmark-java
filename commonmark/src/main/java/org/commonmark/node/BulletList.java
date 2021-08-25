package org.commonmark.node;

public class BulletList extends ListBlock {
	// Track whitespace as follows:
    //    [0] Pre-block
    //    [1] Pre-content
    //    [2] Post-content
    //    [3] Post-block
    private String[] whitespaceTracker = {"", "", "", ""};

    private char bulletMarker;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public char getBulletMarker() {
        return bulletMarker;
    }

    public void setBulletMarker(char bulletMarker) {
        this.bulletMarker = bulletMarker;
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
