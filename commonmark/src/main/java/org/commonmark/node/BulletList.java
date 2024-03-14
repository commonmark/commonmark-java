package org.commonmark.node;

public class BulletList extends ListBlock {

    private String marker;

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return the bullet list marker that was used, e.g. {@code -}, {@code *} or {@code +}, if available, or null otherwise
     */
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    /**
     * @deprecated use {@link #getMarker()} instead
     */
    @Deprecated
    public char getBulletMarker() {
        return marker != null && !marker.isEmpty() ? marker.charAt(0) : '\0';
    }

    /**
     * @deprecated use {@link #getMarker()} instead
     */
    @Deprecated
    public void setBulletMarker(char bulletMarker) {
        this.marker = bulletMarker != '\0' ? String.valueOf(bulletMarker) : null;
    }
}
