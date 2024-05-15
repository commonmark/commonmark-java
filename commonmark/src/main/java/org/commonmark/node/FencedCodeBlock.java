package org.commonmark.node;

public class FencedCodeBlock extends Block {

    private String fenceCharacter;
    private Integer openingFenceLength;
    private Integer closingFenceLength;
    private int fenceIndent;

    private String info;
    private String literal;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return the fence character that was used, e.g. {@code `} or {@code ~}, if available, or null otherwise
     */
    public String getFenceCharacter() {
        return fenceCharacter;
    }

    public void setFenceCharacter(String fenceCharacter) {
        this.fenceCharacter = fenceCharacter;
    }

    /**
     * @return the length of the opening fence (how many of {{@link #getFenceCharacter()}} were used to start the code
     * block) if available, or null otherwise
     */
    public Integer getOpeningFenceLength() {
        return openingFenceLength;
    }

    public void setOpeningFenceLength(Integer openingFenceLength) {
        if (openingFenceLength != null && openingFenceLength < 3) {
            throw new IllegalArgumentException("openingFenceLength needs to be >= 3");
        }
        checkFenceLengths(openingFenceLength, closingFenceLength);
        this.openingFenceLength = openingFenceLength;
    }

    /**
     * @return the length of the closing fence (how many of {@link #getFenceCharacter()} were used to end the code
     * block) if available, or null otherwise
     */
    public Integer getClosingFenceLength() {
        return closingFenceLength;
    }

    public void setClosingFenceLength(Integer closingFenceLength) {
        if (closingFenceLength != null && closingFenceLength < 3) {
            throw new IllegalArgumentException("closingFenceLength needs to be >= 3");
        }
        checkFenceLengths(openingFenceLength, closingFenceLength);
        this.closingFenceLength = closingFenceLength;
    }

    public int getFenceIndent() {
        return fenceIndent;
    }

    public void setFenceIndent(int fenceIndent) {
        this.fenceIndent = fenceIndent;
    }

    /**
     * @see <a href="http://spec.commonmark.org/0.31.2/#info-string">CommonMark spec</a>
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

    /**
     * @deprecated use {@link #getFenceCharacter()} instead
     */
    @Deprecated
    public char getFenceChar() {
        return fenceCharacter != null && !fenceCharacter.isEmpty() ? fenceCharacter.charAt(0) : '\0';
    }

    /**
     * @deprecated use {@link #setFenceCharacter} instead
     */
    @Deprecated
    public void setFenceChar(char fenceChar) {
        this.fenceCharacter = fenceChar != '\0' ? String.valueOf(fenceChar) : null;
    }

    /**
     * @deprecated use {@link #getOpeningFenceLength} instead
     */
    @Deprecated
    public int getFenceLength() {
        return openingFenceLength != null ? openingFenceLength : 0;
    }

    /**
     * @deprecated use {@link #setOpeningFenceLength} instead
     */
    @Deprecated
    public void setFenceLength(int fenceLength) {
        this.openingFenceLength = fenceLength != 0 ? fenceLength : null;
    }

    private static void checkFenceLengths(Integer openingFenceLength, Integer closingFenceLength) {
        if (openingFenceLength != null && closingFenceLength != null) {
            if (closingFenceLength < openingFenceLength) {
                throw new IllegalArgumentException("fence lengths required to be: closingFenceLength >= openingFenceLength");
            }
        }
    }
}
