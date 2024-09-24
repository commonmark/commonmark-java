package org.commonmark.renderer.text;

import org.commonmark.internal.renderer.text.BulletListHolder;
import org.commonmark.internal.renderer.text.ListHolder;
import org.commonmark.internal.renderer.text.OrderedListHolder;
import org.commonmark.node.*;
import org.commonmark.renderer.NodeRenderer;

import java.util.Set;

/**
 * The node renderer that renders all the core nodes (comes last in the order of node renderers).
 */
public class CoreTextContentNodeRenderer extends AbstractVisitor implements NodeRenderer {

    protected final TextContentNodeRendererContext context;
    private final TextContentWriter textContent;

    private ListHolder listHolder;

    public CoreTextContentNodeRenderer(TextContentNodeRendererContext context) {
        this.context = context;
        this.textContent = context.getWriter();
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
        return Set.of(
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
        );
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
        // LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        textContent.write('\u00AB');
        visitChildren(blockQuote);
        textContent.resetBlock();
        // RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        textContent.write('\u00BB');

        textContent.block();
    }

    @Override
    public void visit(BulletList bulletList) {
        textContent.pushTight(bulletList.isTight());
        listHolder = new BulletListHolder(listHolder, bulletList);
        visitChildren(bulletList);
        textContent.popTight();
        textContent.block();
        listHolder = listHolder.getParent();
    }

    @Override
    public void visit(Code code) {
        textContent.write('\"');
        textContent.write(code.getLiteral());
        textContent.write('\"');
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        var literal = stripTrailingNewline(fencedCodeBlock.getLiteral());
        if (stripNewlines()) {
            textContent.writeStripped(literal);
        } else {
            textContent.write(literal);
        }
        textContent.block();
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        if (stripNewlines()) {
            textContent.whitespace();
        } else {
            textContent.line();
        }
    }

    @Override
    public void visit(Heading heading) {
        visitChildren(heading);
        if (stripNewlines()) {
            textContent.write(": ");
        } else {
            textContent.block();
        }
    }

    @Override
    public void visit(ThematicBreak thematicBreak) {
        if (!stripNewlines()) {
            textContent.write("***");
        }
        textContent.block();
    }

    @Override
    public void visit(HtmlInline htmlInline) {
        writeText(htmlInline.getLiteral());
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        writeText(htmlBlock.getLiteral());
    }

    @Override
    public void visit(Image image) {
        writeLink(image, image.getTitle(), image.getDestination());
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        var literal = stripTrailingNewline(indentedCodeBlock.getLiteral());
        if (stripNewlines()) {
            textContent.writeStripped(literal);
        } else {
            textContent.write(literal);
        }
        textContent.block();
    }

    @Override
    public void visit(Link link) {
        writeLink(link, link.getTitle(), link.getDestination());
    }

    @Override
    public void visit(ListItem listItem) {
        if (listHolder != null && listHolder instanceof OrderedListHolder) {
            OrderedListHolder orderedListHolder = (OrderedListHolder) listHolder;
            String indent = stripNewlines() ? "" : orderedListHolder.getIndent();
            textContent.write(indent + orderedListHolder.getCounter() + orderedListHolder.getDelimiter() + " ");
            visitChildren(listItem);
            textContent.block();
            orderedListHolder.increaseCounter();
        } else if (listHolder != null && listHolder instanceof BulletListHolder) {
            BulletListHolder bulletListHolder = (BulletListHolder) listHolder;
            if (!stripNewlines()) {
                textContent.write(bulletListHolder.getIndent() + bulletListHolder.getMarker() + " ");
            }
            visitChildren(listItem);
            textContent.block();
        }
    }

    @Override
    public void visit(OrderedList orderedList) {
        textContent.pushTight(orderedList.isTight());
        listHolder = new OrderedListHolder(listHolder, orderedList);
        visitChildren(orderedList);
        textContent.popTight();
        textContent.block();
        listHolder = listHolder.getParent();
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        textContent.block();
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        if (stripNewlines()) {
            textContent.whitespace();
        } else {
            textContent.line();
        }
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

    private void writeText(String text) {
        if (stripNewlines()) {
            textContent.writeStripped(text);
        } else {
            textContent.write(text);
        }
    }

    private void writeLink(Node node, String title, String destination) {
        boolean hasChild = node.getFirstChild() != null;
        boolean hasTitle = title != null && !title.equals(destination);
        boolean hasDestination = destination != null && !destination.equals("");

        if (hasChild) {
            textContent.write('"');
            visitChildren(node);
            textContent.write('"');
            if (hasTitle || hasDestination) {
                textContent.whitespace();
                textContent.write('(');
            }
        }

        if (hasTitle) {
            textContent.write(title);
            if (hasDestination) {
                textContent.colon();
                textContent.whitespace();
            }
        }

        if (hasDestination) {
            textContent.write(destination);
        }

        if (hasChild && (hasTitle || hasDestination)) {
            textContent.write(')');
        }
    }

    private boolean stripNewlines() {
        return context.lineBreakRendering() == LineBreakRendering.STRIP;
    }

    private static String stripTrailingNewline(String s) {
        if (s.endsWith("\n")) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }
}
