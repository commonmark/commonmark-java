package org.commonmark.node;

public class ListBlock extends Block {

    private ListType type;
    private char orderedDelimiter;
    private int orderedStart;
    private char bulletMarker;
    private boolean tight;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public ListType getListType() {
        return type;
    }

    public void setListType(ListType type) {
        this.type = type;
    }

    public char getOrderedDelimiter() {
        return orderedDelimiter;
    }

    public void setOrderedDelimiter(char orderedDelimiter) {
        this.orderedDelimiter = orderedDelimiter;
    }

    public int getOrderedStart() {
        return orderedStart;
    }

    public void setOrderedStart(int orderedStart) {
        this.orderedStart = orderedStart;
    }

    public char getBulletMarker() {
        return bulletMarker;
    }

    public void setBulletMarker(char bulletMarker) {
        this.bulletMarker = bulletMarker;
    }

    public boolean isTight() {
        return tight;
    }

    public void setTight(boolean tight) {
        this.tight = tight;
    }

    // TODO: Split into two classes instead?
    public enum ListType {
        BULLET,
        ORDERED
    }

}
