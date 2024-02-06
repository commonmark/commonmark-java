package org.commonmark.renderer.markdown;

import org.commonmark.internal.util.AsciiMatcher;
import org.commonmark.internal.util.CharMatcher;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 * <p>
 * Note that while sometimes it would be easier to record what kind of syntax was used on parsing (e.g. ATX vs Setext
 * heading), this renderer is intended to also work for documents that were created by directly creating
 * {@link Node Nodes} instead. So in order to support that, it sometimes needs to do a bit more work.
 */
public class CoreMarkdownNodeRenderer extends AbstractVisitor implements NodeRenderer {

    private final AsciiMatcher textEscape =
            AsciiMatcher.builder().anyOf("[]<>`*&\n\\").build();
    private final CharMatcher textEscapeInHeading =
            AsciiMatcher.builder(textEscape).anyOf("#").build();
    private final CharMatcher linkDestinationNeedsAngleBrackets =
            AsciiMatcher.builder().c(' ').c('(').c(')').c('<').c('>').c('\n').c('\\').build();
    private final CharMatcher linkDestinationEscapeInAngleBrackets =
            AsciiMatcher.builder().c('<').c('>').c('\n').c('\\').build();
    private final CharMatcher linkTitleEscapeInQuotes =
            AsciiMatcher.builder().c('"').c('\n').c('\\').build();

    private final Pattern orderedListMarkerPattern = Pattern.compile("^([0-9]{1,9})([.)])");

    protected final MarkdownNodeRendererContext context;
    private final MarkdownWriter writer;
    /**
     * If we're currently within a {@link BulletList} or {@link OrderedList}, this keeps the context of that list.
     * It has a parent field so that it can represent a stack (for nested lists).
     */
    private ListHolder listHolder;

    public CoreMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
                BlockQuote.class,
                BulletList.class,
                Code.class,
                Document.class,
                Emphasis.class,
                FencedCodeBlock.class,
                HardLineBreak.class,
                Heading.class,
                HtmlBlock.class,
                HtmlInline.class,
                Image.class,
                IndentedCodeBlock.class,
                Link.class,
                ListItem.class,
                OrderedList.class,
                Paragraph.class,
                SoftLineBreak.class,
                StrongEmphasis.class,
                Text.class,
                ThematicBreak.class
        ));
    }

    @Override
    public void render(Node node) {
        node.accept(this);
    }

    @Override
    public void visit(Document document) {
        // No rendering itself
        visitChildren(document);
        writer.line();
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        writer.raw("***");
        writer.block();
    }

    @Override
    public void visit(Heading heading) {
        if (heading.getLevel() <= 2) {
            LineBreakVisitor lineBreakVisitor = new LineBreakVisitor();
            heading.accept(lineBreakVisitor);
            boolean isMultipleLines = lineBreakVisitor.hasLineBreak();

            if (isMultipleLines) {
                // Setext headings: Can have multiple lines, but only level 1 or 2
                visitChildren(heading);
                writer.line();
                if (heading.getLevel() == 1) {
                    // Note that it would be nice to match the length of the contents instead of just using 3, but that's
                    // not easy.
                    writer.raw("===");
                } else {
                    writer.raw("---");
                }
                writer.block();
                return;
            }
        }

        // ATX headings: Can't have multiple lines, but up to level 6.
        for (int i = 0; i < heading.getLevel(); i++) {
            writer.raw('#');
        }
        writer.raw(' ');
        visitChildren(heading);

        writer.block();
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        String literal = indentedCodeBlock.getLiteral();
        // We need to respect line prefixes which is why we need to write it line by line (e.g. an indented code block
        // within a block quote)
        writer.writePrefix("    ");
        writer.pushPrefix("    ");
        List<String> lines = getLines(literal);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            writer.raw(line);
            if (i != lines.size() - 1) {
                writer.line();
            }
        }
        writer.popPrefix();
        writer.block();
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        String literal = fencedCodeBlock.getLiteral();
        String fence = repeat(String.valueOf(fencedCodeBlock.getFenceChar()), fencedCodeBlock.getFenceLength());
        int indent = fencedCodeBlock.getFenceIndent();

        if (indent > 0) {
            String indentPrefix = repeat(" ", indent);
            writer.writePrefix(indentPrefix);
            writer.pushPrefix(indentPrefix);
        }

        writer.raw(fence);
        if (fencedCodeBlock.getInfo() != null) {
            writer.raw(fencedCodeBlock.getInfo());
        }
        writer.line();
        if (!literal.isEmpty()) {
            List<String> lines = getLines(literal);
            for (String line : lines) {
                writer.raw(line);
                writer.line();
            }
        }
        writer.raw(fence);
        if (indent > 0) {
            writer.popPrefix();
        }
        writer.block();
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        List<String> lines = getLines(htmlBlock.getLiteral());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            writer.raw(line);
            if (i != lines.size() - 1) {
                writer.line();
            }
        }
        writer.block();
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        writer.block();
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        writer.writePrefix("> ");
        writer.pushPrefix("> ");
        visitChildren(blockQuote);
        writer.popPrefix();
        writer.block();
    }

    @Override
    public void visit(BulletList bulletList) {
        writer.pushTight(bulletList.isTight());
        listHolder = new BulletListHolder(listHolder, bulletList);
        visitChildren(bulletList);
        listHolder = listHolder.parent;
        writer.popTight();
        writer.block();
    }

    @Override
    public void visit(OrderedList orderedList) {
        writer.pushTight(orderedList.isTight());
        listHolder = new OrderedListHolder(listHolder, orderedList);
        visitChildren(orderedList);
        listHolder = listHolder.parent;
        writer.popTight();
        writer.block();
    }

    @Override
    public void visit(ListItem listItem) {
        int contentIndent = listItem.getContentIndent();
        boolean pushedPrefix = false;
        if (listHolder instanceof BulletListHolder) {
            BulletListHolder bulletListHolder = (BulletListHolder) listHolder;
            String marker = repeat(" ", listItem.getMarkerIndent()) + bulletListHolder.bulletMarker;
            writer.writePrefix(marker);
            writer.writePrefix(repeat(" ", contentIndent - marker.length()));
            writer.pushPrefix(repeat(" ", contentIndent));
            pushedPrefix = true;
        } else if (listHolder instanceof OrderedListHolder) {
            OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
            String marker = repeat(" ", listItem.getMarkerIndent()) + orderedListHolder.number + orderedListHolder.delimiter;
            orderedListHolder.number++;
            writer.writePrefix(marker);
            writer.writePrefix(repeat(" ", contentIndent - marker.length()));
            writer.pushPrefix(repeat(" ", contentIndent));
            pushedPrefix = true;
        }
        if (listItem.getFirstChild() == null) {
            // Empty list item
            writer.block();
        } else {
            visitChildren(listItem);
        }
        if (pushedPrefix) {
            writer.popPrefix();
        }
    }

    @Override
    public void visit(Code code) {
        String literal = code.getLiteral();
        // If the literal includes backticks, we can surround them by using one more backtick.
        int backticks = findMaxRunLength('`', literal);
        for (int i = 0; i < backticks + 1; i++) {
            writer.raw('`');
        }
        // If the literal starts or ends with a backtick, surround it with a single space.
        // If it starts and ends with a space (but is not only spaces), add an additional space (otherwise they would
        // get removed on parsing).
        boolean addSpace = literal.startsWith("`") || literal.endsWith("`") ||
                (literal.startsWith(" ") && literal.endsWith(" ") && Parsing.hasNonSpace(literal));
        if (addSpace) {
            writer.raw(' ');
        }
        writer.raw(literal);
        if (addSpace) {
            writer.raw(' ');
        }
        for (int i = 0; i < backticks + 1; i++) {
            writer.raw('`');
        }
    }

    @Override
    public void visit(Emphasis emphasis) {
        // When emphasis is nested, a different delimiter needs to be used
        char delimiter = writer.getLastChar() == '*' ? '_' : '*';
        writer.raw(delimiter);
        super.visit(emphasis);
        writer.raw(delimiter);
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        writer.raw("**");
        super.visit(strongEmphasis);
        writer.raw("**");
    }

    @Override
    public void visit(Link link) {
        writeLinkLike(link.getTitle(), link.getDestination(), link, "[");
    }

    @Override
    public void visit(Image image) {
        writeLinkLike(image.getTitle(), image.getDestination(), image, "![");
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        writer.raw(htmlInline.getLiteral());
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        writer.raw("  ");
        writer.line();
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        writer.line();
    }

    @Override
    public void visit(Text text) {
        // Text is tricky. In Markdown special characters (`-`, `#` etc.) can be escaped (`\-`, `\#` etc.) so that
        // they're parsed as plain text. Currently, whether a character was escaped or not is not recorded in the Node,
        // so here we don't know. If we just wrote out those characters unescaped, the resulting Markdown would change
        // meaning (turn into a list item, heading, etc.).
        // You might say "Why not store that in the Node when parsing", but that wouldn't work for the use case where
        // nodes are constructed directly instead of via parsing. This renderer needs to work for that too.
        // So currently, when in doubt, we escape. For special characters only occurring at the beginning of a line,
        // we only escape them then (we wouldn't want to escape every `.` for example).
        String literal = text.getLiteral();
        if (writer.isAtLineStart() && !literal.isEmpty()) {
            char c = literal.charAt(0);
            switch (c) {
                case '-': {
                    // Would be ambiguous with a bullet list marker, escape
                    writer.raw("\\-");
                    literal = literal.substring(1);
                    break;
                }
                case '#': {
                    // Would be ambiguous with an ATX heading, escape
                    writer.raw("\\#");
                    literal = literal.substring(1);
                    break;
                }
                case '=': {
                    // Would be ambiguous with a Setext heading, escape
                    writer.raw("\\=");
                    literal = literal.substring(1);
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    // Check for ordered list marker
                    Matcher m = orderedListMarkerPattern.matcher(literal);
                    if (m.find()) {
                        writer.raw(m.group(1));
                        writer.raw("\\" + m.group(2));
                        literal = literal.substring(m.end());
                    }
                    break;
                }
                case '\t': {
                    writer.raw("&#9;");
                    literal = literal.substring(1);
                    break;
                }
                case ' ': {
                    writer.raw("&#32;");
                    literal = literal.substring(1);
                    break;
                }
            }
        }

        CharMatcher escape = text.getParent() instanceof Heading ? textEscapeInHeading : textEscape;

        if (literal.endsWith("!") && text.getNext() instanceof Link) {
            // If we wrote the `!` unescaped, it would turn the link into an image instead.
            writer.text(literal.substring(0, literal.length() - 1), escape);
            writer.raw("\\!");
        } else {
            writer.text(literal, escape);
        }
    }

    @Override
    protected void visitChildren(Node parent) {
        Node node = parent.getFirstChild();
        while (node != null) {
            Node next = node.getNext();
            context.render(node);
            node = next;
        }
    }

    private static int findMaxRunLength(char c, CharSequence s) {
        int backticks = 0;
        int start = 0;
        while (start < s.length()) {
            int index = Parsing.find(c, s, start);
            if (index != -1) {
                start = Parsing.skip(c, s, index + 1, s.length());
                backticks = Math.max(backticks, start - index);
            } else {
                break;
            }
        }
        return backticks;
    }

    private static boolean contains(String s, CharMatcher charMatcher) {
        for (int i = 0; i < s.length(); i++) {
            if (charMatcher.matches(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private static List<String> getLines(String literal) {
        // Without -1, split would discard all trailing empty strings, which is not what we want, e.g. it would
        // return the same result for "abc", "abc\n" and "abc\n\n".
        // With -1, it returns ["abc"], ["abc", ""] and ["abc", "", ""].
        String[] parts = literal.split("\n", -1);
        if (parts[parts.length - 1].isEmpty()) {
            // But we don't want the last empty string, as "\n" is used as a line terminator (not a separator),
            // so return without the last element.
            return Arrays.asList(parts).subList(0, parts.length - 1);
        } else {
            return Arrays.asList(parts);
        }
    }

    private void writeLinkLike(String title, String destination, Node node, String opener) {
        writer.raw(opener);
        visitChildren(node);
        writer.raw(']');
        writer.raw('(');
        if (contains(destination, linkDestinationNeedsAngleBrackets)) {
            writer.raw('<');
            writer.text(destination, linkDestinationEscapeInAngleBrackets);
            writer.raw('>');
        } else {
            writer.raw(destination);
        }
        if (title != null) {
            writer.raw(' ');
            writer.raw('"');
            writer.text(title, linkTitleEscapeInQuotes);
            writer.raw('"');
        }
        writer.raw(')');
    }

    private static class ListHolder {
        final ListHolder parent;

        protected ListHolder(ListHolder parent) {
            this.parent = parent;
        }
    }

    private static class BulletListHolder extends ListHolder {
        final char bulletMarker;

        public BulletListHolder(ListHolder parent, BulletList bulletList) {
            super(parent);
            this.bulletMarker = bulletList.getBulletMarker();
        }
    }

    private static class OrderedListHolder extends ListHolder {
        final char delimiter;
        private int number;

        protected OrderedListHolder(ListHolder parent, OrderedList orderedList) {
            super(parent);
            delimiter = orderedList.getDelimiter();
            number = orderedList.getStartNumber();
        }
    }

    /**
     * Visits nodes to check if there are any soft or hard line breaks.
     */
    private static class LineBreakVisitor extends AbstractVisitor {
        private boolean lineBreak = false;

        public boolean hasLineBreak() {
            return lineBreak;
        }

        @Override
        public void visit(SoftLineBreak softLineBreak) {
            super.visit(softLineBreak);
            lineBreak = true;
        }

        @Override
        public void visit(HardLineBreak hardLineBreak) {
            super.visit(hardLineBreak);
            lineBreak = true;
        }
    }
}
