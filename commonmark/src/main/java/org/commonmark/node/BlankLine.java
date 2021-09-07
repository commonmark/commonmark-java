package org.commonmark.node;

public class BlankLine extends Block {

    private String raw;

    public BlankLine(String rawContent) {
        raw = rawContent;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
