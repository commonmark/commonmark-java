package org.commonmark.parser.beta;

import org.commonmark.node.Node;
import org.commonmark.node.Text;

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
 * Images use the same syntax as links but with a {@code !} in front, e.g. {@code ![text](destination)}.
 */
public interface LinkInfo {
    enum OpenerType {
        /**
         * An image (a {@code !} before the {@code [})
         */
        IMAGE,
        /**
         * A link
         */
        LINK
    }

    /**
     * The type of opener of this link/image:
     * <ul>
     * <li>{@link OpenerType#LINK} for links like {@code [text...}</li>
     * <li>{@link OpenerType#IMAGE} for images like {@code ![text...}</li>
     * </ul>
     */
    OpenerType openerType();

    /**
     * The text node of the opening bracket {@code [}.
     */
    Text openingBracket();

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
     * The position after the closing text bracket, e.g.:
     * <pre>
     * [foo][bar]
     *      ^
     * </pre>
     */
    Position afterTextBracket();
}
