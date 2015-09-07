package org.commonmark.spannable;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BlockQuote;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Header;
import org.commonmark.node.HorizontalRule;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Link;
import org.commonmark.node.ListBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Node;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;

import android.text.TextUtils;

public class SpannableVisitor extends AbstractVisitor {
    private final SpannableWriter mSpannableWriter;

    public SpannableVisitor(SpannableWriter spannableWriter) {
        mSpannableWriter = spannableWriter;
    }

    @Override
    public void visit(BlockQuote blockQuote) {
        mSpannableWriter.blockQuote();
        visitChildren(blockQuote);
        addParagraphIfNeeded(blockQuote);
    }

    @Override
    public void visit(BulletList bulletList) {
        mSpannableWriter.unorderedList();
        visitChildren(bulletList);
        addParagraphIfNeeded(bulletList);
    }

    @Override
    public void visit(Code code) {
        mSpannableWriter.code();
        mSpannableWriter.write(code.getLiteral());
    }

    @Override
    public void visit(Emphasis emphasis) {
        mSpannableWriter.italic();
        visitChildren(emphasis);
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        if (!TextUtils.isEmpty(fencedCodeBlock.getInfo())) {
            mSpannableWriter.italic();
            mSpannableWriter.bold();
            mSpannableWriter.write(fencedCodeBlock.getInfo());
            mSpannableWriter.line();
        }
        mSpannableWriter.codeBlock();
        mSpannableWriter.write(fencedCodeBlock.getLiteral());
        addParagraphIfNeeded(fencedCodeBlock);
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        mSpannableWriter.line();
        visitChildren(hardLineBreak);
    }

    @Override
    public void visit(Header header) {
        mSpannableWriter.header();
        visitChildren(header);
        addParagraphIfNeeded(header);
    }

    @Override
    public void visit(HorizontalRule horizontalRule) {
        visitChildren(horizontalRule);
        mSpannableWriter.line();
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        visitChildren(htmlBlock);
        mSpannableWriter.line();
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        mSpannableWriter.codeBlock();
        mSpannableWriter.write(indentedCodeBlock.getLiteral());
        addParagraphIfNeeded(indentedCodeBlock);
    }

    @Override
    public void visit(Link link) {
        mSpannableWriter.link(link.getDestination());
        visitChildren(link);
    }

    @Override
    public void visit(ListItem listItem) {
        mSpannableWriter.listItem();
        visitChildren(listItem);
        mSpannableWriter.line();
    }

    @Override
    public void visit(OrderedList orderedList) {
        mSpannableWriter.orderedList();
        mSpannableWriter.resetCount();
        visitChildren(orderedList);
        addParagraphIfNeeded(orderedList);
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        if (!isInTightList(paragraph)) {
            addParagraphIfNeeded(paragraph);
        }
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        mSpannableWriter.line();
        visitChildren(softLineBreak);
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        mSpannableWriter.bold();
        visitChildren(strongEmphasis);
    }

    @Override
    public void visit(Text text) {
        mSpannableWriter.write(text.getLiteral());
        visitChildren(text);
    }

    private void addParagraphIfNeeded(Node node) {
        if (node.getNext() != null) {
            mSpannableWriter.paragraph();
        }
    }

    private boolean isInTightList(Paragraph paragraph) {
        Node parent = paragraph.getParent();
        if (parent != null) {
            Node gramps = parent.getParent();
            if (gramps != null && gramps instanceof ListBlock) {
                ListBlock list = (ListBlock) gramps;
                return list.isTight();
            }
        }
        return false;
    }
}
