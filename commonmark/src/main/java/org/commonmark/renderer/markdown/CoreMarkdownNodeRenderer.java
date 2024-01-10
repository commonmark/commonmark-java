package org.commonmark.renderer.markdown;

import org.commonmark.internal.util.AsciiMatcher;
import org.commonmark.internal.util.CharMatcher;
import org.commonmark.internal.util.Parsing;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreMarkdownNodeRenderer extends AbstractVisitor implements NodeRenderer {

    private final CharMatcher linkDestinationNeedsAngleBrackets =
            AsciiMatcher.builder().c(' ').c('(').c(')').c('<').c('>').c('\\').build();
    private final CharMatcher linkDestinationEscapeInAngleBrackets =
            AsciiMatcher.builder().c('<').c('>').build();
    private final CharMatcher linkTitleEscapeInQuotes =
            AsciiMatcher.builder().c('"').build();

    protected final MarkdownNodeRendererContext context;
    private final MarkdownWriter writer;

    public CoreMarkdownNodeRenderer(MarkdownNodeRendererContext context) {
        this.context = context;
        this.writer = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return new HashSet<>(Arrays.asList(
                Document.class,
                Heading.class,
                Paragraph.class,
                BlockQuote.class,
                BulletList.class,
                FencedCodeBlock.class,
                HtmlBlock.class,
                ThematicBreak.class,
                IndentedCodeBlock.class,
                Link.class,
                ListItem.class,
                OrderedList.class,
                Image.class,
                Emphasis.class,
                StrongEmphasis.class,
                Text.class,
                Code.class,
                HtmlInline.class,
                SoftLineBreak.class,
                HardLineBreak.class
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
    public void visit(BlockQuote blockQuote) {
        writer.write("> ");
        writer.pushPrefix("> ");
        visitChildren(blockQuote);
        writer.popPrefix();
        writer.block();
    }

    @Override
    public void visit(BulletList bulletList) {
    }

    @Override
    public void visit(Code code) {
        String literal = code.getLiteral();
        // If the literal includes backticks, we can surround them by using one more backtick.
        int backticks = findMaxRunLength('`', literal);
        for (int i = 0; i < backticks + 1; i++) {
            writer.write('`');
        }
        // If the literal starts or ends with a backtick, surround it with a single space.
        boolean addSpace = literal.startsWith("`") || literal.endsWith("`");
        if (addSpace) {
            writer.write(' ');
        }
        writer.write(literal);
        if (addSpace) {
            writer.write(' ');
        }
        for (int i = 0; i < backticks + 1; i++) {
            writer.write('`');
        }
    }

    @Override
    public void visit(Emphasis emphasis) {
        // When emphasis is nested, a different delimiter needs to be used
        char delimiter = writer.getLastChar() == '*' ? '_' : '*';
        writer.write(delimiter);
        super.visit(emphasis);
        writer.write(delimiter);
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        writer.write("  ");
        writer.line();
    }

    @Override
    public void visit(Heading heading) {
        for (int i = 0; i < heading.getLevel(); i++) {
            writer.write('#');
        }
        writer.write(' ');
        visitChildren(heading);
        writer.block();
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        writer.write("***");
        writer.block();
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        writer.write(htmlInline.getLiteral());
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        writer.write(htmlBlock.getLiteral());
        writer.block();
    }

    @Override
    public void visit(Image image) {
        writeLinkLike(image.getTitle(), image.getDestination(), image, "![");
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        String literal = indentedCodeBlock.getLiteral();
        String[] lines = literal.split("\n");
        // We need to respect line prefixes which is why we need to write it line by line (e.g. an indented code block
        // within a block quote)
        writer.pushPrefix("    ");
        writer.write("    ");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            writer.write(line);
            if (i != lines.length - 1) {
                writer.line();
            }
        }
        writer.popPrefix();
        writer.block();
    }

    @Override
    public void visit(Link link) {
        writeLinkLike(link.getTitle(), link.getDestination(), link, "[");
    }

    @Override
    public void visit(ListItem listItem) {
    }

    @Override
    public void visit(OrderedList orderedList) {
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        writer.block();
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        writer.line();
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        writer.write("**");
        super.visit(strongEmphasis);
        writer.write("**");
    }

    @Override
    public void visit(Text text) {
        writeText(text.getLiteral());
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

    private void writeText(String text) {
        writer.write(text);
    }

    private void writeLinkLike(String title, String destination, Node node, String opener) {
        writer.write(opener);
        visitChildren(node);
        writer.write(']');
        writer.write('(');
        if (contains(destination, linkDestinationNeedsAngleBrackets)) {
            writer.write('<');
            writer.writeEscaped(destination, linkDestinationEscapeInAngleBrackets);
            writer.write('>');
        } else {
            writer.write(destination);
        }
        if (title != null) {
            writer.write(' ');
            writer.write('"');
            writer.writeEscaped(title, linkTitleEscapeInQuotes);
            writer.write('"');
        }
        writer.write(')');
    }
}
