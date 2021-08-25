package org.commonmark.internal.renderer.text;

import org.commonmark.node.OrderedList;

public class OrderedListHolder extends ListHolder {
    private final char delimiter;
    private int counter;
    private String rawNumber;

    public OrderedListHolder(ListHolder parent, OrderedList list) {
        super(parent);
        delimiter = list.getDelimiter();
        counter = list.getStartNumber();
        rawNumber = list.getRawNumber();
    }

    public char getDelimiter() {
        return delimiter;
    }

    public int getCounter() {
        return counter;
    }
    
    public String getRawNumber() {
        return rawNumber;
    }

    public void increaseCounter() {
        counter++;
    }
}
