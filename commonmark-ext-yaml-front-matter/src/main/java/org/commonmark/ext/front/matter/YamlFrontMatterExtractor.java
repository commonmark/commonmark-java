package org.commonmark.ext.front.matter;

import org.commonmark.parser.block.BlockContinue;

public interface YamlFrontMatterExtractor {
    void onNextLine(YamlFrontMatterBlock block, CharSequence line);

    BlockContinue onBlockEnd(YamlFrontMatterBlock block);

    interface Factory {
        YamlFrontMatterExtractor create();
    }
}
