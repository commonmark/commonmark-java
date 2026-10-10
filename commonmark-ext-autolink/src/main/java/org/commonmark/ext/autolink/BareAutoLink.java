package org.commonmark.ext.autolink;

import org.commonmark.node.CustomNode;
import org.commonmark.node.Link;
import org.commonmark.node.Visitor;

/**
 * A bare autolink detected in prose text by the autolink extension — a URL, email address, or
 * www-prefixed domain that was linkified without explicit angle-bracket markup.
 *
 * <p>Examples in a document (with the autolink extension enabled):
 *
 * <pre><code>
 * Visit http://example.com for details.
 * Contact us at foo@example.com.
 * See www.example.com for more.
 * </code></pre>
 *
 * <p>The node carries the original text as it appeared in the source:
 *
 * <ul>
 *   <li>{@link #getContent()} returns the literal text, e.g. {@code "http://example.com"},
 *       {@code "foo@example.com"}, or {@code "www.example.com"}
 *   <li>{@link #getDestination()} derives the href: email gets {@code "mailto:"} prefix,
 *       www gets {@code "http://"} prefix, URL is verbatim
 *   <li>{@link #getAutolinkType()} identifies which kind of bare link this is
 * </ul>
 *
 * <p>Unlike spec {@link org.commonmark.node.AutoLink}, bare autolinks have no angle brackets and no
 * children.
 */
public class BareAutoLink extends CustomNode implements Link {

    private String content;
    private AutolinkType autolinkType;

    public BareAutoLink(String content, AutolinkType autolinkType) {
        this.content = content;
        this.autolinkType = autolinkType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return the literal text as it appeared in the source, e.g. {@code "http://example.com"},
     *     {@code "foo@example.com"}, or {@code "www.example.com"}
     */
    public String getContent() {
        return content;
    }

    /** @return the type of bare autolink detected */
    public AutolinkType getAutolinkType() {
        return autolinkType;
    }

    /**
     * @return the link destination; email addresses are prefixed with {@code "mailto:"}, www links
     *     with {@code "http://"}, and URL links are returned verbatim
     */
    @Override
    public String getDestination() {
        if (autolinkType == AutolinkType.EMAIL) {
            return "mailto:" + content;
        } else if (autolinkType == AutolinkType.WWW) {
            return "http://" + content;
        } else {
            return content;
        }
    }

    /** @return always {@code null}, bare autolinks have no title */
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected String toStringAttributes() {
        return "content=" + content + ", autolinkType=" + autolinkType;
    }
}
