package org.commonmark.ext.front.matter.parser;

import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterRawContent;
import org.commonmark.parser.SourceLine;

public class RawContentParser implements FrontMatterParser {
    private StringBuilder content;

    public RawContentParser() {
        content = new StringBuilder();
    }

    @Override
    public void onNextLine(YamlFrontMatterBlock block, SourceLine line) {
        content.append(line.getContent()).append('\n');
    }

    @Override
    public SeparatorRole onEndingSeparator(YamlFrontMatterBlock block, SourceLine separator) {
        block.appendChild(new YamlFrontMatterRawContent(content.toString()));
        return SeparatorRole.BLOCK_END;
    }

    public static class Factory implements FrontMatterParser.Factory {
        @Override
        public FrontMatterParser create() {
            return new RawContentParser();
        }
    }
}
