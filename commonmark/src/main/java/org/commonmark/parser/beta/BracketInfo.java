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

    String text();

    String label();

    Position afterTextBracket();
}
