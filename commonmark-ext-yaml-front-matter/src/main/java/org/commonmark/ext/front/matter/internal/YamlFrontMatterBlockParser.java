package org.commonmark.ext.front.matter.internal;

import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.node.Block;
import org.commonmark.node.Document;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlFrontMatterBlockParser extends AbstractBlockParser {
    private static final Pattern REGEX_METADATA = Pattern.compile("^[ ]{0,3}([A-Za-z0-9._-]+):\\s*(.*)");
    private static final Pattern REGEX_METADATA_LIST = Pattern.compile("^[ ]+-\\s*(.*)");
    private static final Pattern REGEX_METADATA_LITERAL = Pattern.compile("^\\s*(.*)");
    private static final Pattern REGEX_BEGIN = Pattern.compile("^-{3}(\\s.*)?");
    private static final Pattern REGEX_END = Pattern.compile("^(-{3}|\\.{3})(\\s.*)?");

    private boolean inLiteral;
    private String currentKey;
    private List<String> currentValues;
    private YamlFrontMatterBlock block;

    public YamlFrontMatterBlockParser() {
        inLiteral = false;
        currentKey = null;
        currentValues = new ArrayList<>();
        block = new YamlFrontMatterBlock();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public void addLine(SourceLine line) {
    }

    @Override
    public BlockContinue tryContinue(ParserState parserState) {
        final CharSequence line = parserState.getLine().getContent();

        if (REGEX_END.matcher(line).matches()) {
            if (currentKey != null) {
                block.appendChild(new YamlFrontMatterNode(currentKey, currentValues));
            }
            return BlockContinue.finished();
        }

        Matcher matcher = REGEX_METADATA.matcher(line);
        if (matcher.matches()) {
            if (currentKey != null) {
                block.appendChild(new YamlFrontMatterNode(currentKey, currentValues));
            }

            inLiteral = false;
            currentKey = matcher.group(1);
            currentValues = new ArrayList<>();
            String value = matcher.group(2);
            if ("|".equals(value)) {
                inLiteral = true;
            } else if (!"".equals(value)) {
                currentValues.add(parseString(value));
            }

            return BlockContinue.atIndex(parserState.getIndex());
        } else {
            if (inLiteral) {
                matcher = REGEX_METADATA_LITERAL.matcher(line);
                if (matcher.matches()) {
                    if (currentValues.size() == 1) {
                        currentValues.set(0, currentValues.get(0) + "\n" + matcher.group(1).trim());
                    } else {
                        currentValues.add(matcher.group(1).trim());
                    }
                }
            } else {
                matcher = REGEX_METADATA_LIST.matcher(line);
                if (matcher.matches()) {
                    String value = matcher.group(1);
                    currentValues.add(parseString(value));
                }
            }

            return BlockContinue.atIndex(parserState.getIndex());
        }
    }

    @Override
    public void parseInlines(InlineParser inlineParser) {
    }

    private static String parseString(String s) {
        // Limited parsing of https://yaml.org/spec/1.2.2/#73-flow-scalar-styles
        // We assume input is well-formed and otherwise treat it as a plain string. In a real
        // parser, e.g. `'foo` would be invalid because it's missing a trailing `'`.
        if (s.startsWith("'") && s.endsWith("'")) {
            String inner = s.substring(1, s.length() - 1);
            return inner.replace("''", "'");
        } else if (s.startsWith("\"") && s.endsWith("\"")) {
            String inner = s.substring(1, s.length() - 1);
            // Only support escaped `\` and `"`, nothing else.
            return inner
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        } else {
            return s;
        }
    }

    public static class Factory extends AbstractBlockParserFactory {
        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            CharSequence line = state.getLine().getContent();
            BlockParser parentParser = matchedBlockParser.getMatchedBlockParser();
            // check whether this line is the first line of whole document or not
            if (parentParser.getBlock() instanceof Document && parentParser.getBlock().getFirstChild() == null &&
                    REGEX_BEGIN.matcher(line).matches()) {
                return BlockStart.of(new YamlFrontMatterBlockParser()).atIndex(state.getNextNonSpaceIndex());
            }

            return BlockStart.none();
        }
    }
}
