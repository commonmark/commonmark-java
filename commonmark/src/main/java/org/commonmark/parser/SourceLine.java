package org.commonmark.parser;

import org.commonmark.node.SourceSpan;

import java.util.Objects;

/**
 * A line or part of a line from the input source.
 *
 * @since 0.16.0
 */
public class SourceLine {

    private final CharSequence content;
    private final SourceSpan sourceSpan;

    public static SourceLine of(CharSequence content, SourceSpan sourceSpan) {
        return new SourceLine(content, sourceSpan);
    }

    private SourceLine(CharSequence content, SourceSpan sourceSpan) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.sourceSpan = sourceSpan;
    }

    public CharSequence getContent() {
        return content;
    }

    public SourceSpan getSourceSpan() {
        return sourceSpan;
    }

    public SourceLine substring(int beginIndex, int endIndex) {
        CharSequence newContent = content.subSequence(beginIndex, endIndex);
        SourceSpan newSourceSpan = null;
        if (sourceSpan != null) {
            int length = endIndex - beginIndex;
            if (length != 0) {
                int columnIndex = sourceSpan.getColumnIndex() + beginIndex;
                int inputIndex = sourceSpan.getInputIndex() + beginIndex;
                newSourceSpan = SourceSpan.of(sourceSpan.getLineIndex(), columnIndex, inputIndex, length);
            }
        }
        return SourceLine.of(newContent, newSourceSpan);
    }
}
