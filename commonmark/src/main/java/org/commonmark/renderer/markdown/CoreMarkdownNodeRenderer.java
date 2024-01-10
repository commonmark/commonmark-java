package org.commonmark.renderer.markdown;

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
    }

    @Override
    public void visit(BlockQuote blockQuote) {
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
    public void visit(FencedCodeBlock fencedCodeBlock) {
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
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
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
    }

    @Override
    public void visit(Image image) {
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
    }

    @Override
    public void visit(Link link) {
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

    private void writeText(String text) {
        writer.write(text);
    }
}
