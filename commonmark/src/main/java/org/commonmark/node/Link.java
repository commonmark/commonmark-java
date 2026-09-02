package org.commonmark.node;

/**
 * A link; the link text is in child nodes.
 *
 * <p>Implementations represent the different syntactic forms a link can take:
 *
 * <ul>
 *   <li>{@link InlineLink} &ndash; {@code [text](/uri "title")}
 *   <li>{@link ReferenceLink} &ndash; {@code [text][label]}, {@code [text][]}, {@code [text]}
 *   <li>{@link AutoLink} &ndash; {@code <https://example.com>}
 * </ul>
 *
 * @see <a href="http://spec.commonmark.org/0.31.2/#links">CommonMark Spec</a>
 */
public interface Link {

    /** The link destination (URL), e.g. {@code /uri}. */
    String getDestination();

    /**
     * The link title, or {@code null} if not present. This is the optional tooltip text, e.g.
     * {@code "title"} in {@code [text](/uri "title")}.
     */
    String getTitle();
}
