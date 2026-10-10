package org.commonmark.node;

/**
 * An image; the alt text is in child nodes.
 *
 * <p>Implementations represent the different syntactic forms an image can take:
 *
 * <ul>
 *   <li>{@link InlineImage} &ndash; {@code ![text](/uri "title")}
 *   <li>{@link ReferenceImage} &ndash; {@code ![text][label]}, {@code ![text][]}, {@code ![text]}
 * </ul>
 *
 * @see <a href="https://spec.commonmark.org/0.31.2/#images">CommonMark Spec</a>
 */
public interface Image {

    /** The image destination (URL), e.g. {@code /uri}. */
    String getDestination();

    /**
     * The image title, or {@code null} if not present. This is the optional tooltip text, e.g.
     * {@code "title"} in {@code ![text](/uri "title")}.
     */
    String getTitle();
}
