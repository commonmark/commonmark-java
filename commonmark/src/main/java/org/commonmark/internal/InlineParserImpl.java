package org.commonmark.internal;

import org.commonmark.internal.inline.Scanner;
import org.commonmark.internal.inline.*;
import org.commonmark.internal.util.Escaping;
import org.commonmark.internal.util.LinkScanner;
import org.commonmark.node.*;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.*;
import java.util.regex.Pattern;

public class InlineParserImpl implements InlineParser, InlineParserState {

    private static final String ASCII_PUNCTUATION = "!\"#\\$%&'\\(\\)\\*\\+,\\-\\./:;<=>\\?@\\[\\\\\\]\\^_`\\{\\|\\}~";
    private static final Pattern PUNCTUATION = Pattern
            .compile("^[" + ASCII_PUNCTUATION + "\\p{Pc}\\p{Pd}\\p{Pe}\\p{Pf}\\p{Pi}\\p{Po}\\p{Ps}]");

    private static final Pattern UNICODE_WHITESPACE_CHAR = Pattern.compile("^[\\p{Zs}\t\r\n\f]");

    private final BitSet specialCharacters;
    private final BitSet delimiterCharacters;
    private final Map<Character, DelimiterProcessor> delimiterProcessors;
    private final InlineParserContext context;
    private final Map<Character, List<InlineContentParser>> inlineParsers;

    private String input;
    private int index;

    /**
     * Top delimiter (emphasis, strong emphasis or custom emphasis). (Brackets are on a separate stack, different
     * from the algorithm described in the spec.)
     */
    private Delimiter lastDelimiter;

    /**
     * Top opening bracket (<code>[</code> or <code>![)</code>).
     */
    private Bracket lastBracket;

    public InlineParserImpl(InlineParserContext inlineParserContext) {
        this.delimiterProcessors = calculateDelimiterProcessors(inlineParserContext.getCustomDelimiterProcessors());

        this.context = inlineParserContext;
        this.inlineParsers = new HashMap<>();
        this.inlineParsers.put('\n', Collections.<InlineContentParser>singletonList(new LineBreakInlineContentParser()));
        this.inlineParsers.put('\\', Collections.<InlineContentParser>singletonList(new BackslashInlineParser()));
        this.inlineParsers.put('`', Collections.<InlineContentParser>singletonList(new BackticksInlineParser()));
        this.inlineParsers.put('&', Collections.<InlineContentParser>singletonList(new EntityInlineParser()));
        this.inlineParsers.put('<', Arrays.asList(new AutolinkInlineParser(), new HtmlInlineParser()));

        this.delimiterCharacters = calculateDelimiterCharacters(this.delimiterProcessors.keySet());
        this.specialCharacters = calculateSpecialCharacters(delimiterCharacters, inlineParsers.keySet());
    }

    public static BitSet calculateDelimiterCharacters(Set<Character> characters) {
        BitSet bitSet = new BitSet();
        for (Character character : characters) {
            bitSet.set(character);
        }
        return bitSet;
    }

    public static BitSet calculateSpecialCharacters(BitSet delimiterCharacters, Set<Character> characters) {
        BitSet bitSet = new BitSet();
        bitSet.or(delimiterCharacters);
        for (Character c : characters) {
            bitSet.set(c);
        }
        bitSet.set('[');
        bitSet.set(']');
        bitSet.set('!');
        return bitSet;
    }

    public static Map<Character, DelimiterProcessor> calculateDelimiterProcessors(List<DelimiterProcessor> delimiterProcessors) {
        Map<Character, DelimiterProcessor> map = new HashMap<>();
        addDelimiterProcessors(Arrays.<DelimiterProcessor>asList(new AsteriskDelimiterProcessor(), new UnderscoreDelimiterProcessor()), map);
        addDelimiterProcessors(delimiterProcessors, map);
        return map;
    }

    // TODO: The implementation shouldn't be public
    @Override
    public Scanner scanner() {
        return new Scanner(input, index);
    }

    private static void addDelimiterProcessors(Iterable<DelimiterProcessor> delimiterProcessors, Map<Character, DelimiterProcessor> map) {
        for (DelimiterProcessor delimiterProcessor : delimiterProcessors) {
            char opening = delimiterProcessor.getOpeningCharacter();
            char closing = delimiterProcessor.getClosingCharacter();
            if (opening == closing) {
                DelimiterProcessor old = map.get(opening);
                if (old != null && old.getOpeningCharacter() == old.getClosingCharacter()) {
                    StaggeredDelimiterProcessor s;
                    if (old instanceof StaggeredDelimiterProcessor) {
                        s = (StaggeredDelimiterProcessor) old;
                    } else {
                        s = new StaggeredDelimiterProcessor(opening);
                        s.add(old);
                    }
                    s.add(delimiterProcessor);
                    map.put(opening, s);
                } else {
                    addDelimiterProcessorForChar(opening, delimiterProcessor, map);
                }
            } else {
                addDelimiterProcessorForChar(opening, delimiterProcessor, map);
                addDelimiterProcessorForChar(closing, delimiterProcessor, map);
            }
        }
    }

    private static void addDelimiterProcessorForChar(char delimiterChar, DelimiterProcessor toAdd, Map<Character, DelimiterProcessor> delimiterProcessors) {
        DelimiterProcessor existing = delimiterProcessors.put(delimiterChar, toAdd);
        if (existing != null) {
            throw new IllegalArgumentException("Delimiter processor conflict with delimiter char '" + delimiterChar + "'");
        }
    }

    /**
     * Parse content in block into inline children, using reference map to resolve references.
     */
    @Override
    public void parse(String content, Node block) {
        reset(content.trim());

        Node previous = null;
        while (true) {
            Node node = parseInline(previous);
            previous = node;
            if (node != null) {
                block.appendChild(node);
            } else {
                break;
            }
        }

        processDelimiters(null);
        mergeChildTextNodes(block);
    }

    void setPosition(Position position) {
        index = position.getIndex();
    }

    void reset(String content) {
        this.input = content;
        this.index = 0;
        this.lastDelimiter = null;
        this.lastBracket = null;
    }

    private Text text(String text) {
        return new Text(text);
    }

    /**
     * Parse the next inline element in subject, advancing our position.
     * On success, return the new inline node.
     * On failure, return null.
     */
    private Node parseInline(Node previous) {
        Scanner scanner = scanner();
        char c = scanner.peek();
        if (c == '\0') {
            return null;
        }

        List<InlineContentParser> inlineParsers = this.inlineParsers.get(c);
        if (inlineParsers != null) {
            for (InlineContentParser inlineParser : inlineParsers) {
                // TODO: Should we pass the whole previous node or can we make the API surface smaller?
                ParsedInline parsedInline = inlineParser.tryParse(this, previous);
                if (parsedInline instanceof ParsedInlineImpl) {
                    ParsedInlineImpl parsedInlineImpl = (ParsedInlineImpl) parsedInline;
                    setPosition(parsedInlineImpl.getPosition());
                    return parsedInlineImpl.getNode();
                }
            }
        }

        Node node;
        switch (c) {
            case '[':
                node = parseOpenBracket();
                break;
            case '!':
                node = parseBang();
                break;
            case ']':
                node = parseCloseBracket();
                break;
            default:
                boolean isDelimiter = delimiterCharacters.get(c);
                if (isDelimiter) {
                    DelimiterProcessor delimiterProcessor = delimiterProcessors.get(c);
                    node = parseDelimiters(delimiterProcessor, c);
                } else {
                    node = parseString();
                }
                break;
        }
        if (node != null) {
            return node;
        } else {
            scanner.next();
            setPosition(scanner.position());
            // When we get here, it's only for a single special character that turned out to not have a special meaning.
            // So we shouldn't have a single surrogate here, hence it should be ok to turn it into a String.
            String literal = String.valueOf(c);
            return text(literal);
        }
    }

    /**
     * Attempt to parse delimiters like emphasis, strong emphasis or custom delimiters.
     */
    private Node parseDelimiters(DelimiterProcessor delimiterProcessor, char delimiterChar) {
        DelimiterData res = scanDelimiters(delimiterProcessor, delimiterChar);
        if (res == null) {
            return null;
        }

        Text node = res.text;

        // Add entry to stack for this opener
        lastDelimiter = new Delimiter(node, delimiterChar, res.canOpen, res.canClose, lastDelimiter);
        lastDelimiter.length = res.count;
        lastDelimiter.originalLength = res.count;
        if (lastDelimiter.previous != null) {
            lastDelimiter.previous.next = lastDelimiter;
        }

        return node;
    }

    /**
     * Add open bracket to delimiter stack and add a text node to block's children.
     */
    private Node parseOpenBracket() {
        Scanner scanner = scanner();
        scanner.next();
        Position start = scanner.position();
        setPosition(start);

        Text node = text("[");

        // Add entry to stack for this opener
        addBracket(Bracket.link(node, start, lastBracket, lastDelimiter));

        return node;
    }

    /**
     * If next character is [, and ! delimiter to delimiter stack and add a text node to block's children.
     * Otherwise just add a text node.
     */
    private Node parseBang() {
        Scanner scanner = scanner();
        scanner.next();
        if (scanner.next('[')) {
            Text node = text("![");

            // Add entry to stack for this opener
            addBracket(Bracket.image(node, scanner.position(), lastBracket, lastDelimiter));

            setPosition(scanner.position());
            return node;
        } else {
            setPosition(scanner.position());
            return text("!");
        }
    }

    /**
     * Try to match close bracket against an opening in the delimiter stack. Return either a link or image, or a
     * plain [ character. If there is a matching delimiter, remove it from the delimiter stack.
     */
    private Node parseCloseBracket() {
        Scanner scanner = scanner();
        Position beforeClose = scanner.position();
        scanner.next();
        setPosition(scanner.position());

        // Get previous `[` or `![`
        Bracket opener = lastBracket;
        if (opener == null) {
            // No matching opener, just return a literal.
            return text("]");
        }

        if (!opener.allowed) {
            // Matching opener but it's not allowed, just return a literal.
            removeLastBracket();
            return text("]");
        }

        // Check to see if we have a link/image
        String dest = null;
        String title = null;

        // Maybe a inline link like `[foo](/uri "title")`
        if (scanner.next('(')) {
            scanner.whitespace();
            dest = parseLinkDestination(scanner);
            if (dest != null) {
                int whitespace = scanner.whitespace();
                // title needs a whitespace before
                if (whitespace >= 1) {
                    title = parseLinkTitle(scanner);
                    scanner.whitespace();
                }
                if (!scanner.next(')')) {
                    // Don't have a closing `)`, so it's not a destination and title -> reset.
                    // Note that something like `[foo](` could be valid, `(` will just be text.
                    scanner = scanner();
                    dest = null;
                    title = null;
                }
            }
        }

        // Maybe a reference link like `[foo][bar]`, `[foo][]` or `[foo]`.
        // Note that even `[foo](` could be a valid link if there's a reference, which is why this is not just an `else`
        // here.
        if (dest == null) {
            // See if there's a link label like `[bar]` or `[]`
            String ref = parseLinkLabel(scanner);
            if ((ref == null || ref.isEmpty()) && !opener.bracketAfter) {
                // If the second label is empty `[foo][]` or missing `[foo]`, then the first label is the reference.
                // But it can only be a reference when there's no (unescaped) bracket in it.
                // If there is, we don't even need to try to look up the reference. This is an optimization.
                ref = scanner.textBetween(opener.contentPosition, beforeClose).toString();
            }

            if (ref != null) {
                String label = Escaping.normalizeLabelContent(ref);
                LinkReferenceDefinition definition = context.getLinkReferenceDefinition(label);
                if (definition != null) {
                    dest = definition.getDestination();
                    title = definition.getTitle();
                }
            }
        }

        if (dest != null) {
            // If we got here, open is a potential opener
            Node linkOrImage = opener.image ? new Image(dest, title) : new Link(dest, title);

            Node node = opener.node.getNext();
            while (node != null) {
                Node next = node.getNext();
                linkOrImage.appendChild(node);
                node = next;
            }

            // Process delimiters such as emphasis inside link/image
            processDelimiters(opener.previousDelimiter);
            mergeChildTextNodes(linkOrImage);
            // We don't need the corresponding text node anymore, we turned it into a link/image node
            opener.node.unlink();
            removeLastBracket();

            // Links within links are not allowed. We found this link, so there can be no other link around it.
            if (!opener.image) {
                Bracket bracket = lastBracket;
                while (bracket != null) {
                    if (!bracket.image) {
                        // Disallow link opener. It will still get matched, but will not result in a link.
                        bracket.allowed = false;
                    }
                    bracket = bracket.previous;
                }
            }

            setPosition(scanner.position());

            return linkOrImage;

        } else {
            // No link or image, parse just the bracket as text and continue
            removeLastBracket();

            return text("]");
        }
    }

    private void addBracket(Bracket bracket) {
        if (lastBracket != null) {
            lastBracket.bracketAfter = true;
        }
        lastBracket = bracket;
    }

    private void removeLastBracket() {
        lastBracket = lastBracket.previous;
    }

    /**
     * Attempt to parse link destination, returning the string or null if no match.
     */
    private String parseLinkDestination(Scanner scanner) {
        char delimiter = scanner.peek();
        Position start = scanner.position();
        if (!LinkScanner.scanLinkDestination(scanner)) {
            return null;
        }

        String dest;
        if (delimiter == '<') {
            // chop off surrounding <..>:
            CharSequence rawDestination = scanner.textBetween(start, scanner.position());
            dest = rawDestination.subSequence(1, rawDestination.length() - 1).toString();
        } else {
            dest = scanner.textBetween(start, scanner.position()).toString();
        }

        return Escaping.unescapeString(dest);
    }

    /**
     * Attempt to parse link title (sans quotes), returning the string or null if no match.
     */
    private String parseLinkTitle(Scanner scanner) {
        Position start = scanner.position();
        if (!LinkScanner.scanLinkTitle(scanner)) {
            return null;
        }

        // chop off ', " or parens
        CharSequence rawTitle = scanner.textBetween(start, scanner.position());
        String title = rawTitle.subSequence(1, rawTitle.length() - 1).toString();
        return Escaping.unescapeString(title);
    }

    /**
     * Attempt to parse a link label, returning the label between the brackets or null.
     */
    String parseLinkLabel(Scanner scanner) {
        if (!scanner.next('[')) {
            return null;
        }

        Position start = scanner.position();
        if (!LinkScanner.scanLinkLabelContent(scanner)) {
            return null;
        }
        Position end = scanner.position();

        if (!scanner.next(']')) {
            return null;
        }

        String content = scanner.textBetween(start, end).toString();
        // spec: A link label can have at most 999 characters inside the square brackets.
        if (content.length() > 999) {
            return null;
        }

        return content;
    }

    /**
     * Parse a run of non-special characters as plain text.
     */
    private Node parseString() {
        Scanner scanner = scanner();
        Position start = scanner.position();
        while (scanner.hasNext()) {
            if (specialCharacters.get(scanner.peek())) {
                break;
            }
            scanner.next();
        }

        String text = scanner.textBetween(start, scanner.position()).toString();
        if (!text.isEmpty()) {
            setPosition(scanner.position());
            return text(text);
        } else {
            return null;
        }
    }

    /**
     * Scan a sequence of characters with code delimiterChar, and return information about the number of delimiters
     * and whether they are positioned such that they can open and/or close emphasis or strong emphasis.
     *
     * @return information about delimiter run, or {@code null}
     */
    private DelimiterData scanDelimiters(DelimiterProcessor delimiterProcessor, char delimiterChar) {
        Scanner scanner = scanner();
        char charBefore = scanner.peekPrevious();
        Position start = scanner.position();

        int delimiterCount = scanner.matchMultiple(delimiterChar);

        if (delimiterCount < delimiterProcessor.getMinLength()) {
            setPosition(start);
            return null;
        }

        char charAfter = scanner.peek();
        String before = charBefore == '\0' ? "\n" : String.valueOf(charBefore);
        String after = charAfter == '\0' ? "\n" : String.valueOf(charAfter);

        // We could be more lazy here, in most cases we don't need to do every match case.
        boolean beforeIsPunctuation = PUNCTUATION.matcher(before).matches();
        boolean beforeIsWhitespace = UNICODE_WHITESPACE_CHAR.matcher(before).matches();
        boolean afterIsPunctuation = PUNCTUATION.matcher(after).matches();
        boolean afterIsWhitespace = UNICODE_WHITESPACE_CHAR.matcher(after).matches();

        boolean leftFlanking = !afterIsWhitespace &&
                (!afterIsPunctuation || beforeIsWhitespace || beforeIsPunctuation);
        boolean rightFlanking = !beforeIsWhitespace &&
                (!beforeIsPunctuation || afterIsWhitespace || afterIsPunctuation);
        boolean canOpen;
        boolean canClose;
        if (delimiterChar == '_') {
            canOpen = leftFlanking && (!rightFlanking || beforeIsPunctuation);
            canClose = rightFlanking && (!leftFlanking || afterIsPunctuation);
        } else {
            canOpen = leftFlanking && delimiterChar == delimiterProcessor.getOpeningCharacter();
            canClose = rightFlanking && delimiterChar == delimiterProcessor.getClosingCharacter();
        }

        setPosition(scanner.position());
        String text = scanner.textBetween(start, scanner.position()).toString();
        return new DelimiterData(delimiterCount, canOpen, canClose, new Text(text));
    }

    private void processDelimiters(Delimiter stackBottom) {

        Map<Character, Delimiter> openersBottom = new HashMap<>();

        // find first closer above stackBottom:
        Delimiter closer = lastDelimiter;
        while (closer != null && closer.previous != stackBottom) {
            closer = closer.previous;
        }
        // move forward, looking for closers, and handling each
        while (closer != null) {
            char delimiterChar = closer.delimiterChar;

            DelimiterProcessor delimiterProcessor = delimiterProcessors.get(delimiterChar);
            if (!closer.canClose || delimiterProcessor == null) {
                closer = closer.next;
                continue;
            }

            char openingDelimiterChar = delimiterProcessor.getOpeningCharacter();

            // Found delimiter closer. Now look back for first matching opener.
            int useDelims = 0;
            boolean openerFound = false;
            boolean potentialOpenerFound = false;
            Delimiter opener = closer.previous;
            while (opener != null && opener != stackBottom && opener != openersBottom.get(delimiterChar)) {
                if (opener.canOpen && opener.delimiterChar == openingDelimiterChar) {
                    potentialOpenerFound = true;
                    useDelims = delimiterProcessor.getDelimiterUse(opener, closer);
                    if (useDelims > 0) {
                        openerFound = true;
                        break;
                    }
                }
                opener = opener.previous;
            }

            if (!openerFound) {
                if (!potentialOpenerFound) {
                    // Set lower bound for future searches for openers.
                    // Only do this when we didn't even have a potential
                    // opener (one that matches the character and can open).
                    // If an opener was rejected because of the number of
                    // delimiters (e.g. because of the "multiple of 3" rule),
                    // we want to consider it next time because the number
                    // of delimiters can change as we continue processing.
                    openersBottom.put(delimiterChar, closer.previous);
                    if (!closer.canOpen) {
                        // We can remove a closer that can't be an opener,
                        // once we've seen there's no matching opener:
                        removeDelimiterKeepNode(closer);
                    }
                }
                closer = closer.next;
                continue;
            }

            Text openerNode = opener.node;
            Text closerNode = closer.node;

            // Remove number of used delimiters from stack and inline nodes.
            opener.length -= useDelims;
            closer.length -= useDelims;
            openerNode.setLiteral(
                    openerNode.getLiteral().substring(0,
                            openerNode.getLiteral().length() - useDelims));
            closerNode.setLiteral(
                    closerNode.getLiteral().substring(0,
                            closerNode.getLiteral().length() - useDelims));

            removeDelimitersBetween(opener, closer);
            // The delimiter processor can re-parent the nodes between opener and closer,
            // so make sure they're contiguous already. Exclusive because we want to keep opener/closer themselves.
            mergeTextNodesBetweenExclusive(openerNode, closerNode);
            delimiterProcessor.process(openerNode, closerNode, useDelims);

            // No delimiter characters left to process, so we can remove delimiter and the now empty node.
            if (opener.length == 0) {
                removeDelimiterAndNode(opener);
            }

            if (closer.length == 0) {
                Delimiter next = closer.next;
                removeDelimiterAndNode(closer);
                closer = next;
            }
        }

        // remove all delimiters
        while (lastDelimiter != null && lastDelimiter != stackBottom) {
            removeDelimiterKeepNode(lastDelimiter);
        }
    }

    private void removeDelimitersBetween(Delimiter opener, Delimiter closer) {
        Delimiter delimiter = closer.previous;
        while (delimiter != null && delimiter != opener) {
            Delimiter previousDelimiter = delimiter.previous;
            removeDelimiterKeepNode(delimiter);
            delimiter = previousDelimiter;
        }
    }

    /**
     * Remove the delimiter and the corresponding text node. For used delimiters, e.g. `*` in `*foo*`.
     */
    private void removeDelimiterAndNode(Delimiter delim) {
        Text node = delim.node;
        node.unlink();
        removeDelimiter(delim);
    }

    /**
     * Remove the delimiter but keep the corresponding node as text. For unused delimiters such as `_` in `foo_bar`.
     */
    private void removeDelimiterKeepNode(Delimiter delim) {
        removeDelimiter(delim);
    }

    private void removeDelimiter(Delimiter delim) {
        if (delim.previous != null) {
            delim.previous.next = delim.next;
        }
        if (delim.next == null) {
            // top of stack
            lastDelimiter = delim.previous;
        } else {
            delim.next.previous = delim.previous;
        }
    }

    private void mergeTextNodesBetweenExclusive(Node fromNode, Node toNode) {
        // No nodes between them
        if (fromNode == toNode || fromNode.getNext() == toNode) {
            return;
        }

        mergeTextNodesInclusive(fromNode.getNext(), toNode.getPrevious());
    }

    private void mergeChildTextNodes(Node node) {
        // No children or just one child node, no need for merging
        if (node.getFirstChild() == node.getLastChild()) {
            return;
        }

        mergeTextNodesInclusive(node.getFirstChild(), node.getLastChild());
    }

    private void mergeTextNodesInclusive(Node fromNode, Node toNode) {
        Text first = null;
        Text last = null;
        int length = 0;

        Node node = fromNode;
        while (node != null) {
            if (node instanceof Text) {
                Text text = (Text) node;
                if (first == null) {
                    first = text;
                }
                length += text.getLiteral().length();
                last = text;
            } else {
                mergeIfNeeded(first, last, length);
                first = null;
                last = null;
                length = 0;
            }
            if (node == toNode) {
                break;
            }
            node = node.getNext();
        }

        mergeIfNeeded(first, last, length);
    }

    private void mergeIfNeeded(Text first, Text last, int textLength) {
        if (first != null && last != null && first != last) {
            StringBuilder sb = new StringBuilder(textLength);
            sb.append(first.getLiteral());
            Node node = first.getNext();
            Node stop = last.getNext();
            while (node != stop) {
                sb.append(((Text) node).getLiteral());
                Node unlink = node;
                node = node.getNext();
                unlink.unlink();
            }
            String literal = sb.toString();
            first.setLiteral(literal);
        }
    }

    private static class DelimiterData {

        final int count;
        final boolean canClose;
        final boolean canOpen;
        final Text text;

        DelimiterData(int count, boolean canOpen, boolean canClose, Text text) {
            this.count = count;
            this.canOpen = canOpen;
            this.canClose = canClose;
            this.text = text;
        }
    }
}
