package org.commonmark.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.commonmark.internal.inline.AsteriskDelimiterProcessor;
import org.commonmark.internal.inline.AutolinkInlineParser;
import org.commonmark.internal.inline.BackslashInlineParser;
import org.commonmark.internal.inline.BackticksInlineParser;
import org.commonmark.internal.inline.EntityInlineParser;
import org.commonmark.internal.inline.HtmlInlineParser;
import org.commonmark.internal.inline.InlineContentParser;
import org.commonmark.internal.inline.InlineParserState;
import org.commonmark.internal.inline.ParsedInline;
import org.commonmark.internal.inline.ParsedInlineImpl;
import org.commonmark.internal.inline.Position;
import org.commonmark.internal.inline.Scanner;
import org.commonmark.internal.inline.UnderscoreDelimiterProcessor;
import org.commonmark.internal.util.Escaping;
import org.commonmark.internal.util.LinkScanner;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.LinkFormat.LinkType;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Node;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.delimiter.DelimiterProcessor;

public class InlineParserImpl implements InlineParser, InlineParserState {

    private final BitSet specialCharacters;
    private final Map<Character, DelimiterProcessor> delimiterProcessors;
    private final InlineParserContext context;
    private final Map<Character, List<InlineContentParser>> inlineParsers;

    private Scanner scanner;
    private boolean includeSourceSpans;
    private int trailingSpaces;

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
        this.inlineParsers.put('\\', Collections.<InlineContentParser>singletonList(new BackslashInlineParser()));
        this.inlineParsers.put('`', Collections.<InlineContentParser>singletonList(new BackticksInlineParser()));
        this.inlineParsers.put('&', Collections.<InlineContentParser>singletonList(new EntityInlineParser()));
        this.inlineParsers.put('<', Arrays.asList(new AutolinkInlineParser(), new HtmlInlineParser()));

        this.specialCharacters = calculateSpecialCharacters(this.delimiterProcessors.keySet(), inlineParsers.keySet());
    }

    public static BitSet calculateSpecialCharacters(Set<Character> delimiterCharacters, Set<Character> characters) {
        BitSet bitSet = new BitSet();
        for (Character c : delimiterCharacters) {
            bitSet.set(c);
        }
        for (Character c : characters) {
            bitSet.set(c);
        }
        bitSet.set('[');
        bitSet.set(']');
        bitSet.set('!');
        bitSet.set('\n');
        return bitSet;
    }

    public static Map<Character, DelimiterProcessor> calculateDelimiterProcessors(List<DelimiterProcessor> delimiterProcessors) {
        Map<Character, DelimiterProcessor> map = new HashMap<>();
        addDelimiterProcessors(Arrays.<DelimiterProcessor>asList(new AsteriskDelimiterProcessor(), new UnderscoreDelimiterProcessor()), map);
        addDelimiterProcessors(delimiterProcessors, map);
        return map;
    }

    @Override
    public Scanner scanner() {
        return scanner;
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
     * Parse content in block into inline children, appending them to the block node.
     */
    @Override
    public void parse(SourceLines lines, Node block) {
        reset(lines);

        while (true) {
            List<? extends Node> nodes = parseInline();
            if (nodes != null) {
                for (Node node : nodes) {
                    block.appendChild(node);
                }
            } else {
                break;
            }
        }

        processDelimiters(null, "");
        mergeChildTextNodes(block);
    }

    void reset(SourceLines lines) {
        this.scanner = Scanner.of(lines);
        this.includeSourceSpans = !lines.getSourceSpans().isEmpty();
        this.trailingSpaces = 0;
        this.lastDelimiter = null;
        this.lastBracket = null;
    }

    // Text node without roundtrip information
    private Text text(SourceLines sourceLines) {
        Text text = new Text(sourceLines.getContent(), sourceLines.getContent(), "", "");
        text.setSourceSpans(sourceLines.getSourceSpans());
        return text;
    }
    
    // Text node with roundtrip information
    private Text text(SourceLines sourceLines, String preContentWhitespace, String postContentWhitespace) {
        Text text = new Text(sourceLines.getContent(), sourceLines.getContent(), preContentWhitespace, postContentWhitespace);
        text.setSourceSpans(sourceLines.getSourceSpans());
        return text;
    }

    /**
     * Parse the next inline element in subject, advancing our position.
     * On success, return the new inline node.
     * On failure, return null.
     */
    private List<? extends Node> parseInline() {
        // AST: Capture raw information needed for roundtrip rendering
        String prefix = scanner.alignToLiteral();
        
        char c = scanner.peek();

        switch (c) {
            case '[':
                return Collections.singletonList(parseOpenBracket());
            case '!':
                return Collections.singletonList(parseBang());
            case ']':
                List<Node> nodeList = Collections.singletonList(parseCloseBracket(prefix));
                prefix = "";
                return nodeList;
            case '\n':
                return Collections.singletonList(parseLineBreak());
            case Scanner.END:
                return null;
        }

        // No inline parser, delimiter or other special handling.
        if (!specialCharacters.get(c)) {
            List<Node> nodeList = Collections.singletonList(parseText(prefix));
            prefix = "";
            return nodeList;
        }

        List<InlineContentParser> inlineParsers = this.inlineParsers.get(c);
        if (inlineParsers != null) {
            Position position = scanner.position();
            for (InlineContentParser inlineParser : inlineParsers) {
                ParsedInline parsedInline;
                if(inlineParser instanceof BackticksInlineParser) {
                    parsedInline = inlineParser.tryParse(this, prefix);
                    prefix = "";
                }else {
                    parsedInline = inlineParser.tryParse(this);
                }
                
                if (parsedInline instanceof ParsedInlineImpl) {
                    ParsedInlineImpl parsedInlineImpl = (ParsedInlineImpl) parsedInline;
                    Node node = parsedInlineImpl.getNode();
                    scanner.setPosition(parsedInlineImpl.getPosition());
                    if (includeSourceSpans && node.getSourceSpans().isEmpty()) {
                        node.setSourceSpans(scanner.getSource(position, scanner.position()).getSourceSpans());
                    }
                    return Collections.singletonList(node);
                } else {
                    // Reset position
                    scanner.setPosition(position);
                }
            }
        }

        DelimiterProcessor delimiterProcessor = delimiterProcessors.get(c);
        if (delimiterProcessor != null) {
            List<? extends Node> nodes = parseDelimiters(delimiterProcessor, c, prefix);
            
            // Preserve a prefix which has _only_ whitespace, as this may be
            //    needed in the following "parseText" method
            if(!prefix.isEmpty() && prefix.trim().equals("")) {
                prefix = "";
            }
            
            if (nodes != null) {
                return nodes;
            }
        }

        // If we get here, even for a special/delimiter character, we will just treat it as text.
        return Collections.singletonList(parseText(prefix));
    }

    /**
     * Attempt to parse delimiters like emphasis, strong emphasis or custom delimiters.
     */
    private List<? extends Node> parseDelimiters(DelimiterProcessor delimiterProcessor, char delimiterChar, String prefix) {
        DelimiterData res = scanDelimiters(delimiterProcessor, delimiterChar, prefix);
        if (res == null) {
            return null;
        }

        List<Text> characters = res.characters;

        // Add entry to stack for this opener
        lastDelimiter = new Delimiter(characters, delimiterChar, res.canOpen, res.canClose, lastDelimiter);
        if (lastDelimiter.previous != null) {
            lastDelimiter.previous.next = lastDelimiter;
        }

        return characters;
    }

    /**
     * Add open bracket to delimiter stack and add a text node to block's children.
     */
    private Node parseOpenBracket() {
        Position start = scanner.position();
        scanner.next();
        Position contentPosition = scanner.position();

        Text node = text(scanner.getSource(start, contentPosition));

        // Add entry to stack for this opener
        addBracket(Bracket.link(node, start, contentPosition, lastBracket, lastDelimiter));

        return node;
    }

    /**
     * If next character is [, and ! delimiter to delimiter stack and add a text node to block's children.
     * Otherwise just add a text node.
     */
    private Node parseBang() {
        Position start = scanner.position();
        scanner.next();
        
        if (scanner.next('[')) {
            Position contentPosition = scanner.position();
            Text node = text(scanner.getSource(start, contentPosition));

            // Add entry to stack for this opener
            addBracket(Bracket.image(node, start, contentPosition, lastBracket, lastDelimiter));
            return node;
        } else {
            return text(scanner.getSource(start, scanner.position()));
        }
    }

    /**
     * Try to match close bracket against an opening in the delimiter stack. Return either a link or image, or a
     * plain [ character. If there is a matching delimiter, remove it from the delimiter stack.
     */
    private Node parseCloseBracket() {
        return parseCloseBracket("");
    }
    
    private Node parseCloseBracket(String prefix) {
        Position beforeClose = scanner.position();
        scanner.next();
        
        Position afterClose = scanner.position();

        // Get previous `[` or `![`
        Bracket opener = lastBracket;
        if (opener == null) {
            // No matching opener, just return a literal.
            return text(scanner.getSource(beforeClose, afterClose));
        }

        if (!opener.allowed) {
            // Matching opener but it's not allowed, just return a literal.
            removeLastBracket();
            return text(scanner.getSource(beforeClose, afterClose));
        }

        // Check to see if we have a link/image
        String dest = null;
        String rawDest = "";
        String title = null;
        String rawTitle = "";
        
        LinkType currentLinkType = LinkType.NULL;
        char titleSymbol = Character.MIN_VALUE;
        String whitespacePreDestination = "";
        String whitespacePreTitle = "";
        String whitespacePostContent = "";

        // Maybe a inline link like `[foo](/uri "title")`
        if (scanner.next('(')) {
            whitespacePreDestination = scanner.whitespaceAsString();
            scanner.whitespace();
            rawDest = parseLinkDestinationRaw(scanner);
            dest = parseLinkDestination(scanner);
            
            if (dest == null) {
                scanner.setPosition(afterClose);
                whitespacePreDestination = "";
            } else {
                whitespacePreTitle = scanner.whitespaceAsString();

                // title needs a whitespace before
                if (whitespacePreTitle.length() >= 1) {
                    Position titleStart = scanner.position();
                    // Capture the symbol used for the title
                    titleSymbol = scanner.peek();
                    rawTitle = parseLinkTitleRaw(scanner);
                    scanner.setPosition(titleStart);
                    title = parseLinkTitle(scanner);
                    
                    // If title isn't valid, discard the captured value of its symbol
                    if(title == null) {
                        titleSymbol = Character.MIN_VALUE;
                    }
                    whitespacePostContent = scanner.whitespaceAsString();
                }
                
                currentLinkType = LinkType.INLINE;
                
                if (!scanner.next(')')) {
                    // Don't have a closing `)`, so it's not a destination and title -> reset.
                    // Note that something like `[foo](` could be valid, `(` will just be text.
                    scanner.setPosition(afterClose);
                    dest = null;
                    rawDest = "";
                    title = null;
                    rawTitle = "";
                    currentLinkType = LinkType.NULL;
                    titleSymbol = Character.MIN_VALUE;
                    whitespacePreDestination = "";
                    whitespacePreTitle = "";
                    whitespacePostContent = "";
                }
            }
        }

        String label = null;
        String rawLabel = null;
        
        // Maybe a reference link like `[foo][bar]`, `[foo][]` or `[foo]`.
        // Note that even `[foo](` could be a valid link if there's a reference, which is why this is not just an `else`
        // here.
        if (dest == null) {
            // See if there's a link label like `[bar]` or `[]`
            String ref = parseLinkLabel(scanner);
            if (ref == null) {
                scanner.setPosition(afterClose);
            }
            if ((ref == null || ref.isEmpty()) && !opener.bracketAfter) {
                // If the second label is empty `[foo][]` or missing `[foo]`, then the first label is the reference.
                // But it can only be a reference when there's no (unescaped) bracket in it.
                // If there is, we don't even need to try to look up the reference. This is an optimization.
                ref = scanner.getSource(opener.contentPosition, beforeClose).getContent();
            }

            if (ref != null) {
                LinkReferenceDefinition definition = context.getLinkReferenceDefinition(ref);
                if (definition != null) {
                    dest = definition.getDestination();
                    title = definition.getTitle();
                    label = definition.getLabel();
                }else {
                    if(!ref.isEmpty()) {
                        label = ref;
                    }
                }
                
                currentLinkType = LinkType.REFERENCE;
                
                // If this reference link has a following label (like
                //    `[foo][bar]` or `[foo][]`, capture it for roundtrip
                //    purposes
                Position currentPos = scanner.position();
                
                scanner.setPosition(beforeClose);
                scanner.next();
                
                whitespacePostContent = scanner.whitespaceAsString();
                
                // Check just in case there's a "[]" hanging out, but if
                //    there are any line breaks ignore extra symbols
                //    because a next pass would catch those.
                if(scanner.hasNext() && scanner.peek() == '[' && !whitespacePostContent.contains("\n")) {
                    scanner.next();
                    if(scanner.peek() == ']') {
                        label = "";
                    }
                }else {
                    label = null;
                }
                
                // The raw label needs to be populated, either with the original (raw)
                //    link reference info or with the (literal) label information
                if(label != null && !label.isEmpty() && !ref.equals(label)) {
                    rawLabel = ref;
                }else {
                    rawLabel = label;
                }
                
                // Reset scanner if no extra content was found
                scanner.setPosition(currentPos);
            }
        }

        if (dest != null) {
            // If we got here, we have a link or image
            Node linkOrImage = opener.image ?
                    new Image(dest, rawDest, title, rawTitle, label, rawLabel, currentLinkType, titleSymbol, whitespacePreDestination, whitespacePreTitle, whitespacePostContent) :
                    new Link(dest, rawDest, title, rawTitle, label, rawLabel, currentLinkType, titleSymbol, whitespacePreDestination, whitespacePreTitle, whitespacePostContent);

            // Add all nodes between the opening bracket and now (closing bracket) as child nodes of the link
            Node node = opener.node.getNext();
            while (node != null) {
                Node next = node.getNext();
                linkOrImage.appendChild(node);
                node = next;
            }

            if (includeSourceSpans) {
                linkOrImage.setSourceSpans(scanner.getSource(opener.markerPosition, scanner.position()).getSourceSpans());
            }

            // Process delimiters such as emphasis inside link/image
            processDelimiters(opener.previousDelimiter, prefix);
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

            return linkOrImage;

        } else {
            // No link or image, parse just the bracket as text and continue
            removeLastBracket();

            scanner.setPosition(afterClose);
            return text(scanner.getSource(beforeClose, afterClose), prefix, "");
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
            String rawDestination = scanner.getSource(start, scanner.position()).getContent();
            dest = rawDestination.substring(1, rawDestination.length() - 1);
        } else {
            dest = scanner.getSource(start, scanner.position()).getContent();
        }

        return Escaping.unescapeString(dest);
    }
    
    private String parseLinkDestinationRaw(Scanner rawScanner) {
        Position start = rawScanner.position();
        if(!LinkScanner.scanLinkDestination(rawScanner)) {
            return null;
        }
        
        String dest = rawScanner.getSource(start, scanner.position()).getContent();
        rawScanner.setPosition(start);
        
        return dest;
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
        String rawTitle = scanner.getSource(start, scanner.position()).getContent();
        String title = rawTitle.substring(1, rawTitle.length() - 1);
        return Escaping.unescapeString(title);
    }
    
    /**
     * Attempt to parse the link title in raw form, returning the raw string or null if no match.
     */
    private String parseLinkTitleRaw(Scanner rawScanner) {
        Position start = rawScanner.position();
        if(!LinkScanner.scanLinkTitle(rawScanner)) {
            return null;
        }
        
        String rawTitle = scanner.getSource(start, scanner.position()).getContent();
        
        return rawTitle;
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

        String content = scanner.getSource(start, end).getContent();
        // spec: A link label can have at most 999 characters inside the square brackets.
        if (content.length() > 999) {
            return null;
        }

        return content;
    }

    private Node parseLineBreak() {
        scanner.next();

        if (trailingSpaces >= 2) {
            return new HardLineBreak();
        } else {
            return new SoftLineBreak();
        }
    }

    /**
     * Parse the next character as plain text, and possibly more if the following characters are non-special.
     */
    private Node parseText() {
        return parseText("");
    }
    
    private Node parseText(String prefix) {
        String preContentWhitespace = "";
        
        if(!prefix.isEmpty()) {
            // Capture whitespace and anything that might be a block quote for roundtrip purposes
            if(prefix.trim().equals("") || prefix.contains(">")) {
                preContentWhitespace = prefix;
            }
        }
        
        StringBuilder postContentWhitespace = new StringBuilder();
        
        Position start = scanner.position();
        scanner.next();

        char c;
        while (true) {
            c = scanner.peek();
            
            if(c == Scanner.END) {
                break;
            }
            
            if(c != ' ' && c != '\t' && c != '\n' && postContentWhitespace.length() > 0) {
                postContentWhitespace.setLength(0);
            }
            
            if (specialCharacters.get(c)) {
                break;
            }
            
            if(c == ' ' || c == '\t') {
                postContentWhitespace.append(c);
            }
            
            scanner.next();
        }
        
        SourceLines source = scanner.getSource(start, scanner.position(), prefix.length());
        String content = source.getContent();

        if (c == '\n') {
            // We parsed until the end of the line. Trim any trailing spaces and remember them (for hard line breaks).
            int end = Parsing.skipBackwards(' ', content, content.length() - 1, 0) + 1;
            trailingSpaces = content.length() - end;
            if(postContentWhitespace.length() == 0) {
                postContentWhitespace.append(content.subSequence(end, content.length()));
            }
            content = content.substring(0, end);
        } else if (c == Scanner.END) {
            // For the last line, both tabs and spaces are trimmed for some reason (checked with commonmark.js).
            int end = Parsing.skipSpaceTabBackwards(content, content.length() - 1, 0) + 1;
            if(postContentWhitespace.length() == 0) {
                postContentWhitespace.append(content.subSequence(end, content.length()));
            }
            content = content.substring(0, end);
        }
        
        Text text = new Text(content, content, preContentWhitespace, postContentWhitespace.toString());
        
        text.setSourceSpans(source.getSourceSpans());
        return text;
    }

    /**
     * Scan a sequence of characters with code delimiterChar, and return information about the number of delimiters
     * and whether they are positioned such that they can open and/or close emphasis or strong emphasis.
     *
     * @return information about delimiter run, or {@code null}
     */
    private DelimiterData scanDelimiters(DelimiterProcessor delimiterProcessor, char delimiterChar, String prefix) {
        int before = scanner.peekPreviousCodePoint();
        Position start = scanner.position();

        // Quick check to see if we have enough delimiters.
        int delimiterCount = scanner.matchMultiple(delimiterChar);
        if (delimiterCount < delimiterProcessor.getMinLength()) {
            scanner.setPosition(start);
            return null;
        }

        // We do have enough, extract a text node for each delimiter character.
        List<Text> delimiters = new ArrayList<>();
        scanner.setPosition(start);
        Position positionBefore = start;
        
        while (scanner.next(delimiterChar)) {
            Text delimText = text(scanner.getSource(positionBefore, scanner.position()), prefix, "");
            
            // Erase prefix after first line
            if(prefix.length() > 0) {
                prefix = "";
            }
            
            delimiters.add(delimText);
            positionBefore = scanner.position();
        }

        int after = scanner.peekCodePoint();

        // We could be more lazy here, in most cases we don't need to do every match case.
        boolean beforeIsPunctuation = before == Scanner.END || Parsing.isPunctuationCodePoint(before);
        boolean beforeIsWhitespace = before == Scanner.END || Parsing.isWhitespaceCodePoint(before);
        boolean afterIsPunctuation = after == Scanner.END || Parsing.isPunctuationCodePoint(after);
        boolean afterIsWhitespace = after == Scanner.END || Parsing.isWhitespaceCodePoint(after);

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

        return new DelimiterData(delimiters, canOpen, canClose);
    }

    private void processDelimiters(Delimiter stackBottom) {
        processDelimiters(stackBottom, "");
    }
    
    private void processDelimiters(Delimiter stackBottom, String prefix) {

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
            if (!closer.canClose() || delimiterProcessor == null) {
                closer = closer.next;
                continue;
            }

            char openingDelimiterChar = delimiterProcessor.getOpeningCharacter();

            // Found delimiter closer. Now look back for first matching opener.
            int usedDelims = 0;
            boolean openerFound = false;
            boolean potentialOpenerFound = false;
            Delimiter opener = closer.previous;
            while (opener != null && opener != stackBottom && opener != openersBottom.get(delimiterChar)) {
                if (opener.canOpen() && opener.delimiterChar == openingDelimiterChar) {
                    potentialOpenerFound = true;
                    usedDelims = delimiterProcessor.process(opener, closer, prefix);
                    
                    String openerWhitespace = opener.characters.get(0).whitespacePreContent();
                    if(!openerWhitespace.trim().equals("")) {
                        closer.characters.get(0).setWhitespace(openerWhitespace, "");
                    }
                    
                    if (usedDelims > 0) {
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
                    if (!closer.canOpen()) {
                        // We can remove a closer that can't be an opener,
                        // once we've seen there's no matching opener:
                        removeDelimiterKeepNode(closer);
                    }
                }
                closer = closer.next;
                continue;
            }

            // Remove number of used delimiters nodes.
            for (int i = 0; i < usedDelims; i++) {
                Text delimiter = opener.characters.remove(opener.characters.size() - 1);
                delimiter.unlink();
            }
            for (int i = 0; i < usedDelims; i++) {
                Text delimiter = closer.characters.remove(0);
                delimiter.unlink();
            }

            removeDelimitersBetween(opener, closer);

            // No delimiter characters left to process, so we can remove delimiter and the now empty node.
            if (opener.length() == 0) {
                removeDelimiterAndNodes(opener);
            }

            if (closer.length() == 0) {
                Delimiter next = closer.next;
                removeDelimiterAndNodes(closer);
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
    private void removeDelimiterAndNodes(Delimiter delim) {
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

    private void mergeChildTextNodes(Node node) {
        // No children, no need for merging
        if (node.getFirstChild() == null) {
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

                mergeChildTextNodes(node);
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
            StringBuilder sb2 = new StringBuilder(textLength);
            sb.append(first.getLiteral());
            sb2.append(first.getRaw());
            SourceSpans sourceSpans = null;
            if (includeSourceSpans) {
                sourceSpans = new SourceSpans();
                sourceSpans.addAll(first.getSourceSpans());
            }
            Node node = first.getNext();
            Node stop = last.getNext();
            while (node != stop) {
                sb.append(((Text) node).getLiteral());
                sb2.append(((Text)node).getRaw());
                if (sourceSpans != null) {
                    sourceSpans.addAll(node.getSourceSpans());
                }

                Node unlink = node;
                node = node.getNext();
                unlink.unlink();
            }
            String literal = sb.toString();
            String raw = sb2.toString();
            first.setLiteral(literal);
            first.setRaw(raw);
            if (sourceSpans != null) {
                first.setSourceSpans(sourceSpans.getSourceSpans());
            }
        }
    }

    private static class DelimiterData {

        final List<Text> characters;
        final boolean canClose;
        final boolean canOpen;

        DelimiterData(List<Text> characters, boolean canOpen, boolean canClose) {
            this.characters = characters;
            this.canOpen = canOpen;
            this.canClose = canClose;
        }
    }
}
