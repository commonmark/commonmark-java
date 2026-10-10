package org.commonmark.node;

/**
 * A reference image, e.g.:
 *
 * <pre><code>
 * ![alt text][label]
 * </code></pre>
 *
 * <p>There are three syntactic forms, distinguished by {@link #getLabel()}:
 *
 * <ul>
 *   <li><b>Shortcut</b> &ndash; {@code ![text]} &ndash; label is {@code null}
 *   <li><b>Collapsed</b> &ndash; {@code ![text][]} &ndash; label is {@code ""}
 *   <li><b>Full</b> &ndash; {@code ![text][label]} &ndash; label is non-empty
 * </ul>
 *
 * <p>The destination and title are always derived from the {@link LinkReferenceDefinition} that
 * this image resolved to at parse time.
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#images">CommonMark Spec</a>
 */
public class ReferenceImage extends Node implements Image {

    private String label;
    private LinkReferenceDefinition definition;

    public ReferenceImage() {}

    public ReferenceImage(String label, LinkReferenceDefinition definition) {
        this.label = label;
        this.definition = definition;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * The reference label used to look up the {@link LinkReferenceDefinition}, or {@code null} for
     * shortcut reference images.
     *
     * <ul>
     *   <li>{@code null} &ndash; shortcut reference image ({@code ![text]})
     *   <li>{@code ""} &ndash; collapsed reference image ({@code ![text][]})
     *   <li>non-empty &ndash; full reference image ({@code ![text][label]})
     * </ul>
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the {@link LinkReferenceDefinition} that this image resolved to at parse time, or
     *     {@code null} if the node was constructed programmatically without one
     */
    public LinkReferenceDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(LinkReferenceDefinition definition) {
        this.definition = definition;
    }

    /** @return the destination from the resolved definition, or {@code null} if none is set */
    @Override
    public String getDestination() {
        return definition != null ? definition.getDestination() : null;
    }

    /** @return the title from the resolved definition, or {@code null} if none is set */
    @Override
    public String getTitle() {
        return definition != null ? definition.getTitle() : null;
    }

    @Override
    protected String toStringAttributes() {
        return "label=" + label + ", destination=" + getDestination() + ", title=" + getTitle();
    }
}
