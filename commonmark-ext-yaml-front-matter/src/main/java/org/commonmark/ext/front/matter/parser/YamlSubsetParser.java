package org.commonmark.ext.front.matter.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterNode;
import org.commonmark.parser.SourceLine;

public class YamlSubsetParser implements FrontMatterParser {
    private static final Pattern REGEX_METADATA =
            Pattern.compile("^[ ]{0,3}([A-Za-z0-9._-]+):\\s*(.*)");
    private static final Pattern REGEX_METADATA_LIST = Pattern.compile("^[ ]+-\\s*(.*)");
    private static final Pattern REGEX_METADATA_LITERAL = Pattern.compile("^\\s*(.*)");

    private boolean inLiteral;
    private String currentKey;
    private List<String> currentValues;

    public YamlSubsetParser() {
        inLiteral = false;
        currentKey = null;
        currentValues = new ArrayList<>();
    }

    @Override
    public void onNextLine(YamlFrontMatterBlock block, SourceLine line) {
        Matcher matcher = REGEX_METADATA.matcher(line.getContent());
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
        } else {
            if (inLiteral) {
                matcher = REGEX_METADATA_LITERAL.matcher(line.getContent());
                if (matcher.matches()) {
                    if (currentValues.size() == 1) {
                        currentValues.set(0, currentValues.get(0) + "\n" + matcher.group(1).trim());
                    } else {
                        currentValues.add(matcher.group(1).trim());
                    }
                }
            } else {
                matcher = REGEX_METADATA_LIST.matcher(line.getContent());
                if (matcher.matches()) {
                    String value = matcher.group(1);
                    currentValues.add(parseString(value));
                }
            }
        }
    }

    @Override
    public SeparatorRole onEndingSeparator(YamlFrontMatterBlock block, SourceLine separator) {
        if (currentKey != null) {
            block.appendChild(new YamlFrontMatterNode(currentKey, currentValues));
        }
        return SeparatorRole.BLOCK_END;
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
            return inner.replace("\\\"", "\"").replace("\\\\", "\\");
        } else {
            return s;
        }
    }

    public static class Factory implements FrontMatterParser.Factory {
        @Override
        public FrontMatterParser create() {
            return new YamlSubsetParser();
        }
    }
}
