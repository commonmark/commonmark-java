package org.commonmark.ext.gfm.strikethrough.internal;

import org.commonmark.ext.gfm.strikethrough.Strikethrough;
import org.commonmark.node.Node;
import org.commonmark.node.Nodes;
import org.commonmark.node.SourceSpans;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class StrikethroughDelimiterProcessor implements DelimiterProcessor {

    private final boolean requireTwoTildes;

    public StrikethroughDelimiterProcessor() {
        this(false);
    }

    public StrikethroughDelimiterProcessor(boolean requireTwoTildes) {
        this.requireTwoTildes = requireTwoTildes;
    }

    @Override
    public char getOpeningCharacter() {
        return '~';
    }

    @Override
    public char getClosingCharacter() {
        return '~';
    }

    @Override
    public int getMinLength() {
        return requireTwoTildes ? 2 : 1;
    }

    @Override
    public int process(DelimiterRun openingRun, DelimiterRun closingRun) {
        if (openingRun.length() == closingRun.length() && openingRun.length() <= 2) {
            // GitHub only accepts either one or two delimiters, but not a mix or more than that.

            Text opener = openingRun.getOpener();

            // Wrap nodes between delimiters in strikethrough.
            String delimiter = openingRun.length() == 1 ? opener.getLiteral() : opener.getLiteral() + opener.getLiteral();
            Node strikethrough = new Strikethrough(delimiter);

            SourceSpans sourceSpans = new SourceSpans();
            sourceSpans.addAllFrom(openingRun.getOpeners(openingRun.length()));

            for (Node node : Nodes.between(opener, closingRun.getCloser())) {
                strikethrough.appendChild(node);
                sourceSpans.addAll(node.getSourceSpans());
            }

            sourceSpans.addAllFrom(closingRun.getClosers(closingRun.length()));
            strikethrough.setSourceSpans(sourceSpans.getSourceSpans());

            opener.insertAfter(strikethrough);

            return openingRun.length();
        } else {
            return 0;
        }
    }
}
