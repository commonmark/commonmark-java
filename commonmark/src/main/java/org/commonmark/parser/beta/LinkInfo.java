package org.commonmark.parser.beta;

/**
 * A parsed link or image. There are different types of links.
 * <p>
 * Inline links:
 * <pre>
 * [text](destination)
 * [text](destination "title")
 * </pre>
 * <p>
 * Reference links, which have different subtypes. Full::
 * <pre>
 * [text][label]
 * </pre>
 * Collapsed (label is ""):
 * <pre>
 * [text][]
 * </pre>
 * Shortcut (label is null):
 * <pre>
 * [text]
 * </pre>
 */
public interface LinkInfo {
    enum OpenerType {
        // An image (a `!` before the `[`)
        IMAGE,
        // A link
        LINK
    }

    // TODO: We could also expose the opener Text (`[` or `![`)
    OpenerType openerType();

    /**
     * The text between the first brackets, e.g. `foo` in `[foo][bar]`.
     */
    String text();

    /**
     * The label, or null for inline links or for shortcut links (in which case {@link #text()} should be used as the label).
     */
    String label();

    /**
     * The destination if available, e.g. in `[foo](destination)`, or null
     */
    String destination();

    /**
     * The title if available, e.g. in `[foo](destination "title")`, or null
     */
    String title();

    /**
     * The position after the text bracket, e.g.:
     * <pre>
     * [foo][bar]
     *      ^
     * </pre>
     */
    Position afterTextBracket();
}
