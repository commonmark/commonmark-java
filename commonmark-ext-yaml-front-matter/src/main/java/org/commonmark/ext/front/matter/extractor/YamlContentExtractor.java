package org.commonmark.ext.front.matter.extractor;

import org.commonmark.ext.front.matter.YamlFrontMatterExtractor;
import org.commonmark.ext.front.matter.YamlFrontMatterBlock;
import org.commonmark.ext.front.matter.YamlFrontMatterContent;
import org.commonmark.parser.block.BlockContinue;

public class YamlContentExtractor implements YamlFrontMatterExtractor {
    private StringBuilder content;

    public YamlContentExtractor() {
        content = new StringBuilder();
    }

    @Override
    public void onNextLine(YamlFrontMatterBlock block, CharSequence line) {
        content.append(line).append('\n');
    }

    @Override
    public BlockContinue onBlockEnd(YamlFrontMatterBlock block) {
        block.appendChild(new YamlFrontMatterContent(content.toString()));
        return BlockContinue.finished();
    }

    public static class Factory implements YamlFrontMatterExtractor.Factory {
        @Override
        public YamlFrontMatterExtractor create() {
            return new YamlContentExtractor();
        }
    }
}
