package org.commonmark.ext.ins.internal;

import org.commonmark.ext.ins.Ins;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class InsDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return '+';
    }

    @Override
    public char getClosingCharacter() {
        return '+';
    }

    @Override
    public int getMinLength() {
        return 2;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        if (openingRun.length() >= 2 && closingRun.length() >= 2) {
            // Use exactly two delimiters even if we have more, and don't care about internal openers/closers.

            Text opener = openingRun.getOpener();

            // Wrap nodes between delimiters in ins.
            Node ins = new Ins();

            SourceSpans sourceSpans = new SourceSpans();
            sourceSpans.addAllFrom(openingRun.getOpeners(2));

            for (Node node : Nodes.between(opener, closingRun.getCloser())) {
                ins.appendChild(node);
                sourceSpans.addAll(node.getSourceSpans());
            }

            sourceSpans.addAllFrom(closingRun.getClosers(2));
            ins.setSourceSpans(sourceSpans.getSourceSpans());

            opener.insertAfter(ins);

            return 2;
        } else {
            return 0;
        }
    }
}
