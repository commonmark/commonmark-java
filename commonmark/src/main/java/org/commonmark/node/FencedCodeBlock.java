package org.commonmark.node;

public class FencedCodeBlock extends Block {

    private char fenceChar;
    private int startFenceLength;
    private int endFenceLength;

    private String info;
    private String literal;
    private String raw;
    
    // Whitespace tracked for roundtrip rendering
    private String whitespacePreStartFence = "";
    private String whitespacePreContent = "";
    private String whitespacePreEndFence = "";
    private String whitespacePostBlock = "";

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
        return whitespacePreStartFence.length();
    }
    
    public int getEndFenceIndent() {
        return whitespacePreEndFence.length();
    }

    /**
     * @see <a href="http://spec.commonmark.org/0.18/#info-string">CommonMark spec</a>
     * @return Info string or null
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
    
    public String whitespacePreStartFence() {
        return whitespacePreStartFence;
    }

    public String whitespacePreContent() {
        return whitespacePreContent;
    }

    public String whitespacePreEndFence() {
        return whitespacePreEndFence;
    }

    public String whitespacePostBlock() {
        return whitespacePostBlock;
    }
    
    public void setPreStartFenceWhitespace(String whitespace) {
        whitespacePreStartFence = whitespace;
    }
    
    public void setPreContentWhitespace(String whitespace) {
        whitespacePreContent = whitespace;
    }
    
    public void setPreEndFenceWhitespace(String whitespace) {
        whitespacePreEndFence = whitespace;
    }
    
    public void setPostBlockWhitespace(String whitespace) {
        whitespacePostBlock = whitespace;
    }
}
