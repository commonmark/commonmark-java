package org.commonmark.internal.inline;

import org.commonmark.internal.util.AsciiMatcher;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Node;

/**
 * Attempt to parse inline HTML.
 */
public class HtmlInlineParser implements InlineContentParser {

    private static final AsciiMatcher asciiLetter = AsciiMatcher.builder().range('A', 'Z').range('a', 'z').build();

    // spec: A tag name consists of an ASCII letter followed by zero or more ASCII letters, digits, or hyphens (-).
    private static final AsciiMatcher tagNameStart = asciiLetter;
    private static final AsciiMatcher tagNameContinue = tagNameStart.newBuilder().range('0', '9').c('-').build();

    // spec: An attribute name consists of an ASCII letter, _, or :, followed by zero or more ASCII letters, digits,
    // _, ., :, or -. (Note: This is the XML specification restricted to ASCII. HTML5 is laxer.)
    private static final AsciiMatcher attributeStart = asciiLetter.newBuilder().c('_').c(':').build();
    private static final AsciiMatcher attributeContinue = attributeStart.newBuilder().range('0', '9').c('.').c('-').build();
    // spec: An unquoted attribute value is a nonempty string of characters not including whitespace, ", ', =, <, >, or `.
    private static final AsciiMatcher attributeValueEnd = AsciiMatcher.builder()
            .c(' ').c('\t').c('\n').c('\u000B').c('\f').c('\r')
            .c('"').c('\'').c('=').c('<').c('>').c('`')
            .build();

    private static final AsciiMatcher declaration = AsciiMatcher.builder().range('A', 'Z').build();

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState, Node previous) {
        Scanner scanner = inlineParserState.scanner();
        Position start = scanner.position();
        // Skip over `<`
        scanner.skip();

        char c = scanner.peek();
        if (tagNameStart.matches(c)) {
            if (tryOpenTag(scanner)) {
                return htmlInline(start, scanner);
            }
        } else if (c == '/') {
            if (tryClosingTag(scanner)) {
                return htmlInline(start, scanner);
            }
        } else if (c == '?') {
            if (tryProcessingInstruction(scanner)) {
                return htmlInline(start, scanner);
            }
        } else if (c == '!') {
            // comment, declaration or CDATA
            scanner.skip();
            c = scanner.peek();
            if (c == '-') {
                if (tryComment(scanner)) {
                    return htmlInline(start, scanner);
                }
            } else if (c == '[') {
                if (tryCdata(scanner)) {
                    return htmlInline(start, scanner);
                }
            } else if (declaration.matches(c)) {
                if (tryDeclaration(scanner)) {
                    return htmlInline(start, scanner);
                }
            }
        }

        return ParsedInline.none();
    }

    private static ParsedInline htmlInline(Position start, Scanner scanner) {
        HtmlInline node = new HtmlInline();
        node.setLiteral(scanner.textBetween(start, scanner.position()));
        return ParsedInline.of(node, scanner.position());
    }

    private static boolean tryOpenTag(Scanner scanner) {
        // spec: An open tag consists of a < character, a tag name, zero or more attributes, optional whitespace,
        // an optional / character, and a > character.
        scanner.skip();
        scanner.skip(tagNameContinue);
        boolean whitespace = scanner.skipWhitespace() >= 1;
        // spec: An attribute consists of whitespace, an attribute name, and an optional attribute value specification.
        while (whitespace && scanner.skip(attributeStart) >= 1) {
            scanner.skip(attributeContinue);
            // spec: An attribute value specification consists of optional whitespace, a = character,
            // optional whitespace, and an attribute value.
            whitespace = scanner.skipWhitespace() >= 1;
            if (scanner.skipOne('=')) {
                scanner.skipWhitespace();
                char valueStart = scanner.peek();
                if (valueStart == '\'') {
                    scanner.skip();
                    if (scanner.find('\'') < 0) {
                        return false;
                    }
                    scanner.skip();
                } else if (valueStart == '"') {
                    scanner.skip();
                    if (scanner.find('"') < 0) {
                        return false;
                    }
                    scanner.skip();
                } else {
                    if (scanner.find(attributeValueEnd) <= 0) {
                        return false;
                    }
                }

                // Whitespace is required between attributes
                whitespace = scanner.skipWhitespace() >= 1;
            }
        }

        scanner.skipOne('/');
        return scanner.skipOne('>');
    }

    private static boolean tryClosingTag(Scanner scanner) {
        // spec: A closing tag consists of the string </, a tag name, optional whitespace, and the character >.
        scanner.skip();
        if (scanner.skip(tagNameStart) >= 1) {
            scanner.skip(tagNameContinue);
            scanner.skipWhitespace();
            return scanner.skipOne('>');
        }
        return false;
    }

    private static boolean tryProcessingInstruction(Scanner scanner) {
        // spec: A processing instruction consists of the string <?, a string of characters not including the string ?>,
        // and the string ?>.
        scanner.skip();
        while (scanner.find('?') > 0) {
            scanner.skip();
            if (scanner.skipOne('>')) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryComment(Scanner scanner) {
        // spec: An HTML comment consists of <!-- + text + -->, where text does not start with > or ->, does not end
        // with -, and does not contain --. (See the HTML5 spec.)

        // Skip first `-`
        scanner.skip();
        if (!scanner.skipOne('-')) {
            return false;
        }

        if (scanner.skipOne('>')) {
            return false;
        }

        if (scanner.skipOne('-')) {
            // Can't start with ->
            if (scanner.skipOne('>')) {
                return false;
            }
            // Empty comment
            if (scanner.skipOne('-')) {
                return scanner.skipOne('>');
            }
        }

        while (scanner.find('-') >= 0) {
            if (scanner.skipOne('-') && scanner.skipOne('-')) {
                return scanner.skipOne('>');
            }
        }

        return false;
    }

    private static boolean tryCdata(Scanner scanner) {
        // spec: A CDATA section consists of the string <![CDATA[, a string of characters not including the string ]]>,
        // and the string ]]>.

        // Skip `[`
        scanner.skip();

        if (scanner.skipOne('C') && scanner.skipOne('D') && scanner.skipOne('A') && scanner.skipOne('T') && scanner.skipOne('A')
                && scanner.skipOne('[')) {
            while (scanner.find(']') >= 0) {
                if (scanner.skipOne(']') && scanner.skipOne(']') && scanner.skipOne('>')) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryDeclaration(Scanner scanner) {
        // spec: A declaration consists of the string <!, a name consisting of one or more uppercase ASCII letters,
        // whitespace, a string of characters not including the character >, and the character >.
        scanner.skip(declaration);
        if (scanner.skipWhitespace() <= 0) {
            return false;
        }
        if (scanner.find('>') >= 0) {
            scanner.skip();
            return true;
        }
        return false;
    }
}
