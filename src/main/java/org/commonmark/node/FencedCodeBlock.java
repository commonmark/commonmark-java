package org.commonmark.node;

public class FencedCodeBlock extends Block {

    private char fenceChar;
    private int fenceLength;
    private int fenceOffset;

    private String info;
    private String literal;

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

    public int getFenceLength() {
        return fenceLength;
    }

    public void setFenceLength(int fenceLength) {
        this.fenceLength = fenceLength;
    }

    public int getFenceOffset() {
        return fenceOffset;
    }

    public void setFenceOffset(int fenceOffset) {
        this.fenceOffset = fenceOffset;
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
}
