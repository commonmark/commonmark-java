package org.commonmark.node;

/**
 * An autolink: a URL or email address enclosed in angle brackets that is rendered as a link.
 *
 * <p>Example in a CommonMark document:
 *
 * <pre><code>
 * &lt;http://example.com&gt;
 * &lt;user@example.com&gt;
 * </code></pre>
 *
 * <p>The node carries the raw content between the angle brackets:
 *
 * <ul>
 *   <li>{@link #getContent()} returns {@code "http://example.com"} or {@code "user@example.com"}
 *   <li>{@link #getDestination()} returns the same for URI autolinks, or {@code
 *       "mailto:user@example.com"} for email autolinks
 *   <li>{@link #isEmail()} returns {@code true} for email autolinks
 * </ul>
 *
 * <p>Unlike {@link InlineLink}, an autolink has no children and no title.
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#autolinks">CommonMark Spec</a>
 */
public class AutoLink extends Node implements Link {

    private String content;
    private boolean email;

    public AutoLink(String content, boolean email) {
        this.content = content;
        this.email = email;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * @return the raw content between the angle brackets, e.g. {@code "http://example.com"} or
     *     {@code "user@example.com"}
     */
    public String getContent() {
        return content;
    }

    /** @return true if this is an email autolink, false if it is a URI autolink */
    public boolean isEmail() {
        return email;
    }

    /**
     * @return the link destination; for email autolinks {@code "mailto:"} is prepended to the
     *     content, for URI autolinks the content is returned verbatim
     */
    @Override
    public String getDestination() {
        return email ? "mailto:" + content : content;
    }

    /** @return always {@code null}, autolinks have no title */
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected String toStringAttributes() {
        return "content=" + content + ", email=" + email;
    }
}
