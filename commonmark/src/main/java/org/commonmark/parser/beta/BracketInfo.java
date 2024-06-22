package org.commonmark.parser.beta;

public interface BracketInfo {
    enum OpenerType {
        // An image (a `!` before the `[`)
        IMAGE,
        // A link
        LINK
    }

    enum ReferenceType {
        FULL,
        COLLAPSED,
        SHORTCUT
    }

    // TODO: We could also expose the opener Text (`[` or `![`)
    OpenerType openerType();

    ReferenceType referenceType();

    /**
     * The text between the first brackets, e.g. `foo` in `[foo][bar]`.
     */
    String text();

    /**
     * The label, or null for shortcut links (in which case {@link #text()} should be used as the label).
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

    Position afterTextBracket();
}
