package org.commonmark.clean;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Header;
import org.commonmark.node.HorizontalRule;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.ListItem;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;

import android.text.TextUtils;

public class CleanVisitor extends AbstractVisitor {
    private final CleanWriter mCleanWriter;

    public CleanVisitor(CleanWriter spannableWriter) {
        mCleanWriter = spannableWriter;
    }

    @Override
    public void visit(Code code) {
        mCleanWriter.write(code.getLiteral());
    }

    @Override
    public void visit(FencedCodeBlock fencedCodeBlock) {
        if (!TextUtils.isEmpty(fencedCodeBlock.getInfo())) {
            mCleanWriter.write(fencedCodeBlock.getInfo());
            mCleanWriter.divider();
        }
        mCleanWriter.write(fencedCodeBlock.getLiteral());
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        mCleanWriter.divider();
    }

    @Override
    public void visit(Header header) {
        visitChildren(header);
        mCleanWriter.divider();
    }

    @Override
    public void visit(HorizontalRule horizontalRule) {
        mCleanWriter.divider();
    }

    @Override
    public void visit(HtmlBlock htmlBlock) {
        visitChildren(htmlBlock);
        mCleanWriter.divider();
    }

    @Override
    public void visit(IndentedCodeBlock indentedCodeBlock) {
        mCleanWriter.write(indentedCodeBlock.getLiteral());
    }

    @Override
    public void visit(ListItem listItem) {
        visitChildren(listItem);
        mCleanWriter.divider();
    }

    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        mCleanWriter.divider();
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        mCleanWriter.divider();
    }

    @Override
    public void visit(Text text) {
        mCleanWriter.write(text.getLiteral());
    }
}
