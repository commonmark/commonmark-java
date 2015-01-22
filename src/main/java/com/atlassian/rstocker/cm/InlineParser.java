package com.atlassian.rstocker.cm;

import java.util.Map;

public class InlineParser {
    // Constants for character codes:

    private static final char C_NEWLINE = 10;
    private static final char C_ASTERISK = 42;
    private static final char C_UNDERSCORE = 95;
    private static final char C_BACKTICK = 96;
    private static final char C_OPEN_BRACKET = 91;
    private static final char C_CLOSE_BRACKET = 93;
    private static final char C_LESSTHAN = 60;
    private static final char C_BANG = 33;
    private static final char C_BACKSLASH = 92;
    private static final char C_AMPERSAND = 38;
    private static final char C_OPEN_PAREN = 40;
    private static final char C_COLON = 58;

    private String subject = "";
    // TODO: rename?
    private Delimiter delimiters;
    private int pos = 0;
    private Map<Object, Object> refmap;
//    match: match,
//    peek: peek,
//    spnl: spnl,
//    parseBackticks: parseBackticks,
//    parseBackslash: parseBackslash,
//    parseAutolink: parseAutolink,
//    parseHtmlTag: parseHtmlTag,
//    scanDelims: scanDelims,
//    parseEmphasis: parseEmphasis,
//    parseLinkTitle: parseLinkTitle,
//    parseLinkDestination: parseLinkDestination,
//    parseLinkLabel: parseLinkLabel,
//    parseOpenBracket: parseOpenBracket,
//    parseCloseBracket: parseCloseBracket,
//    parseBang: parseBang,
//    parseEntity: parseEntity,
//    parseString: parseString,
//    parseNewline: parseNewline,
//    parseReference: parseReference,
//    parseInline: parseInline,
//    processEmphasis: processEmphasis,
//    removeDelimiter: removeDelimiter,
//    parse: parseInlines

    // Parse string_content in block into inline children,
    // using refmap to resolve references.
    public void parse(Node block, Map<Object, Object> refmap) {
        this.subject = block.string_content.trim();
        this.pos = 0;
        this.refmap = refmap; // foo: || {};
        this.delimiters = null;
        while (this.parseInline(block)) {
        }
        this.processEmphasis(block, null);
    }

    // Parse the next inline element in subject, advancing subject position.
    // On success, add the result to block's children and return true.
    // On failure, return false.
    private boolean parseInline(Node block) {
        boolean res;
        char c = this.peek();
        if (c == -1) {
            return false;
        }
        switch(c) {
            case C_NEWLINE:
                res = this.parseNewline(block);
                break;
            case C_BACKSLASH:
                res = this.parseBackslash(block);
                break;
            case C_BACKTICK:
                res = this.parseBackticks(block);
                break;
            case C_ASTERISK:
            case C_UNDERSCORE:
                res = this.parseEmphasis(c, block);
                break;
            case C_OPEN_BRACKET:
                res = this.parseOpenBracket(block);
                break;
            case C_BANG:
                res = this.parseBang(block);
                break;
            case C_CLOSE_BRACKET:
                res = this.parseCloseBracket(block);
                break;
            case C_LESSTHAN:
                res = this.parseAutolink(block) || this.parseHtmlTag(block);
                break;
            case C_AMPERSAND:
                res = this.parseEntity(block);
                break;
            default:
                res = this.parseString(block);
                break;
        }
        if (!res) {
            this.pos += 1;
            var textnode = new Node('Text');
            textnode.literal = fromCodePoint(c);
            block.appendChild(textnode);
        }

        return true;
    };

    private static class Delimiter {

        final Node node;
        final Delimiter previous;
        final int index;

        char cc = C_BANG;
        int numdelims = 1;
        Delimiter next;
        // foo2: camelCase these?
        boolean can_open = true;
        boolean can_close = false;
        boolean active = true;

        public Delimiter(Node node, Delimiter previous, int index) {
            this.node = node;
            this.previous = previous;
            this.index = index;
        }
    }
}
