package org.commonmark.node;

public class FencedCodeBlock extends Block {
 // Track whitespace as follows:
    //    [0] Pre-block
    //    [1] Pre-content
    //    [2] Post-content
    //    [3] Post-block
    private String[] whitespaceTracker = {"", "", "", ""};

    private char fenceChar;
    private int startFenceLength;
    private int endFenceLength;

    private String info;
    private String literal;
    private String raw;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public char getFenceChar() {
        return fenceChar;
    }

    public void setFenceChar(char fenceChar) {
        this.fenceChar = fenceChar;
    }

    public int getStartFenceLength() {
        return startFenceLength;
    }

    public void setStartFenceLength(int startFenceLength) {
        this.startFenceLength = startFenceLength;
    }

    public int getEndFenceLength() {
        return endFenceLength;
    }

    public void setEndFenceLength(int endFenceLength) {
        this.endFenceLength = endFenceLength;
    }

    public int getStartFenceIndent() {
        return whitespaceTracker[0].length();
    }
    
    public int getEndFenceIndent() {
        return whitespaceTracker[2].length();
    }

    /**
     * @see <a href="http://spec.commonmark.org/0.18/#info-string">CommonMark spec</a>
     */
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
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
