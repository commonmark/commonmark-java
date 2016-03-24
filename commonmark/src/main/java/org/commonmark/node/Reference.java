package org.commonmark.node;

public abstract class Reference extends Node {

    protected Reference definition;
    protected String destination;
    protected String title;
    protected String label;

    public Reference() {
    }

    public Reference(String destination, String title) {
        this.destination = destination;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * If this entity represents a collapsed or shortcut link, the
     * <code>getReference</code> method should return the link
     * reference definition that the label matches to.
     */
    public Reference getDefinition() {
        return definition;
    }

    public void setDefinition(Reference definition) {
        this.definition = definition;
    }

    @Override
    protected String toStringAttributes() {
        return "destination=" + destination
            + ", title=" + title
            + ", label=" + label
            + ", definition=" + definition;
    }

}
