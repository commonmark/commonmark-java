package org.commonmark.node;

/**
 * A reference link, e.g.:
 *
 * <pre><code>
 * [link text][label]
 * </code></pre>
 *
 * <p>There are three syntactic forms, distinguished by {@link #getLabel()}:
 *
 * <ul>
 *   <li><b>Shortcut</b> &ndash; {@code [text]} &ndash; label is {@code null}
 *   <li><b>Collapsed</b> &ndash; {@code [text][]} &ndash; label is {@code ""}
 *   <li><b>Full</b> &ndash; {@code [text][label]} &ndash; label is non-empty
 * </ul>
 *
 * <p>The destination and title are always derived from the {@link LinkReferenceDefinition} that
 * this link resolved to at parse time.
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#reference-link">CommonMark Spec</a>
 */
public class ReferenceLink extends Node implements Link {

    private String label;
    private LinkReferenceDefinition definition;

    public ReferenceLink() {}

    public ReferenceLink(String label, LinkReferenceDefinition definition) {
        this.label = label;
        this.definition = definition;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * The reference label used to look up the {@link LinkReferenceDefinition}, or {@code null} for
     * shortcut reference links.
     *
     * <ul>
     *   <li>{@code null} &ndash; shortcut reference link ({@code [text]})
     *   <li>{@code ""} &ndash; collapsed reference link ({@code [text][]})
     *   <li>non-empty &ndash; full reference link ({@code [text][label]})
     * </ul>
     */
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the {@link LinkReferenceDefinition} that this link resolved to at parse time, or
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
