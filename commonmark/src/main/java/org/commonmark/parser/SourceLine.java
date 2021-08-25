package org.commonmark.parser;

import java.util.Objects;

import org.commonmark.node.SourceSpan;

/**
 * A line or part of a line from the input source.
 *
 * @since 0.16.0
 */
public class SourceLine {

    private final CharSequence content;
    private final SourceSpan sourceSpan;
    private final int literalIndex;

    public static SourceLine of(CharSequence content, SourceSpan sourceSpan) {
        return new SourceLine(content, sourceSpan);
    }
    
    // The literal index is the beginning of a literal line, as found within a raw line
    public static SourceLine of(CharSequence content, SourceSpan sourceSpan, int literalIndex) {
        return new SourceLine(content, sourceSpan, literalIndex);
    }

    private SourceLine(CharSequence content, SourceSpan sourceSpan) {
        if (content == null) {
            throw new NullPointerException("content must not be null");
        }
        this.content = content;
        this.sourceSpan = sourceSpan;
        this.literalIndex = 0;
    }
    
    private SourceLine(CharSequence content, SourceSpan sourceSpan, int literalIndex) {
        if (content == null) {
            throw new NullPointerException("content must not be null");
        }
        this.content = content;
        this.sourceSpan = sourceSpan;
        this.literalIndex = literalIndex;
    }

    public CharSequence getContent() {
        return content;
    }

    public SourceSpan getSourceSpan() {
        return sourceSpan;
    }
    
    public int getLiteralIndex() {
        return literalIndex;
    }

    public SourceLine substring(int beginIndex, int endIndex) {
        CharSequence newContent = content.subSequence(beginIndex, endIndex);
        SourceSpan newSourceSpan = null;
        if (sourceSpan != null) {
            int columnIndex = sourceSpan.getColumnIndex() + beginIndex;
            int length = endIndex - beginIndex;
            if (length != 0) {
                newSourceSpan = SourceSpan.of(sourceSpan.getLineIndex(), columnIndex, length);
            }
        }
        return SourceLine.of(newContent, newSourceSpan);
    }
    
 // Many lines are passed around as raw strings, with an index value indicating
    //    where the literal string begins. Calling this method returns a SourceLine containing
    //    only the literal portion of the line (if it is a subset of the line) or the line
    //    in full (if they are the same).
    public SourceLine getLiteralLine() {
        if(literalIndex == 0) {
            return SourceLine.of(content, sourceSpan);
        }else {
            SourceSpan newSourceSpan = null;
            if(sourceSpan != null) {
                int columnIndex = sourceSpan.getColumnIndex() - literalIndex;
                int length = content.length() - literalIndex;
                if(length != 0) {
                    newSourceSpan = SourceSpan.of(sourceSpan.getLineIndex(), columnIndex, length);
                }
            }
            return SourceLine.of(content.subSequence(literalIndex, content.length()), newSourceSpan);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, literalIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SourceLine) {
            if(((SourceLine)obj).getContent().toString().equals(content) &&
                    ((SourceLine)obj).getLiteralIndex() == literalIndex) {
                return true;
            }
        }
        
        return false;
    }
}
