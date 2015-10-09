package org.commonmark.ext.metadata.internal;

import org.commonmark.ext.metadata.MetadataBlock;
import org.commonmark.ext.metadata.MetadataNode;
import org.commonmark.node.Block;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.block.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataBlockParser extends AbstractBlockParser {
    private static final Pattern REGEX_METADATA = Pattern.compile("^[ ]{0,3}([A-Za-z0-9_-]+):\\s*(.*)");
    private static final Pattern REGEX_METADATA_LIST = Pattern.compile("^[ ]+-\\s*(.*)");
    private static final Pattern REGEX_METADATA_LITERAL = Pattern.compile("^\\s*(.*)");
    private static final Pattern REGEX_BOUNDARY = Pattern.compile("^(-{3,}|\\.{3,})(\\s*)?");

    private List<String> lines;
    private MetadataBlock block;
    private boolean literal;

    public MetadataBlockParser() {
        lines = new ArrayList<>();
        block = new MetadataBlock();
        literal = false;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public void addLine(CharSequence line) {
        lines.add(line.toString());
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        if (REGEX_BOUNDARY.matcher(parserState.getLine()).matches()) {
            return BlockContinue.finished();
        }
        return BlockContinue.atIndex(parserState.getIndex());
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
        String key = null;
        List<String> values = new ArrayList<>();

        for (String line : lines) {
            Matcher matcher = REGEX_BOUNDARY.matcher(line);
            if (matcher.matches()) {
                continue;
            }

            matcher = REGEX_METADATA.matcher(line);
            if (matcher.matches()) {
                if (key != null) {
                    MetadataNode node = new MetadataNode();
                    node.setKey(key);
                    node.setValues(values);
                    block.appendChild(node);
                }

                literal = false;
                key = matcher.group(1);
                values = new ArrayList<>();
                if ("|".equals(matcher.group(2))) {
                    literal = true;
                } else if (!"".equals(matcher.group(2))) {
                    values.add(matcher.group(2));
                }
            } else {
                matcher = REGEX_METADATA_LIST.matcher(line);
                if (matcher.matches()) {
                    values.add(matcher.group(1));
                } else if (literal) {
                    matcher = REGEX_METADATA_LITERAL.matcher(line);
                    if (matcher.matches()) {
                        if (values.size() == 1) {
                            values.set(0, values.get(0) + "\n" + matcher.group(1).trim());
                        } else {
                            values.add(matcher.group(1).trim());
                        }
                    }
                }
            }
        }

        if (key != null) {
            MetadataNode node = new MetadataNode();
            node.setKey(key);
            node.setValues(values);
            block.appendChild(node);
        }
    }

    public static class Factory extends AbstractBlockParserFactory {
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            String line = state.getLine().toString();
            if (matchedBlockParser.getParagraphStartLine() == null && REGEX_BOUNDARY.matcher(line).matches()) {
                return BlockStart.of(new MetadataBlockParser()).atIndex(state.getNextNonSpaceIndex());
            }

            return BlockStart.none();
        }
    }
}
